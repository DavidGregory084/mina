package org.mina_lang.codegen.jvm;

import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.MutableMap;
import org.mina_lang.codegen.jvm.scopes.*;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.Scope;
import org.mina_lang.common.names.*;
import org.mina_lang.common.types.Sort;
import org.mina_lang.common.types.TypeApply;
import org.mina_lang.common.types.TypeVar;
import org.mina_lang.syntax.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

public class CodeGenerator {
    CodegenEnvironment environment = CodegenEnvironment.empty();

    MutableMap<Named, byte[]> classes = Maps.mutable.empty();

    public void generate(Path destination, NamespaceNode<Attributes> namespace) {
        generateNamespace(namespace);

        classes.forEachKeyValue((name, classData) -> {
            if (name instanceof NamespaceName nsName) {
                var path = Paths.namespacePath(destination, nsName);
                try {
                    if (!Files.exists(path.getParent())) {
                        Files.createDirectories(path.getParent());
                    }
                    Files.write(path, classData);
                } catch (IOException e) {
                    System.err.printf("Exception while writing class data to %s - %s%n", path, e);
                }
            } else if (name instanceof DataName dataName) {
                var path = Paths.dataPath(destination, dataName);
                try {
                    if (!Files.exists(path.getParent())) {
                        Files.createDirectories(path.getParent());
                    }
                    Files.write(path, classData);
                } catch (IOException e) {
                    System.err.printf("Exception while writing class data to %s - %s%n", path, e);
                }
            } else if (name instanceof ConstructorName constrName) {
                var path = Paths.constructorPath(destination, constrName);
                try {
                    if (!Files.exists(path.getParent())) {
                        Files.createDirectories(path.getParent());
                    }
                    Files.write(path, classData);
                } catch (IOException e) {
                    System.err.printf("Exception while writing class data to %s - %s%n", path, e);
                }
            }

            try {
                var reader = new ClassReader(classData);
                var writer = new PrintWriter(System.err);
                var visitor = new TraceClassVisitor(writer);
                reader.accept(visitor, 0);

                var verifier = new ClassReader(classData);
                var verifyWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
                var verifyAdapter = new CheckClassAdapter(verifyWriter, true);
                verifier.accept(verifyAdapter, 0);
            } catch (Exception e) {
                System.err.printf("Exception while verifying class data - %s%n", e);
                e.printStackTrace(System.err);
            }
        });
    }

    Meta<Attributes> updateMetaWith(Meta<Attributes> meta, Sort sort) {
        var attributes = meta.meta().withSort(sort);
        return meta.withMeta(attributes);
    }

    void putTypeDeclaration(Scope<Attributes> scope, Meta<Attributes> meta) {
        var name = (Named) meta.meta().name();
        scope.putType(name.localName(), meta);
        scope.putType(name.canonicalName(), meta);
        return;
    }

    void putValueDeclaration(Scope<Attributes> scope, Meta<Attributes> meta) {
        var name = (Named) meta.meta().name();
        scope.putValue(name.localName(), meta);
        scope.putValue(name.canonicalName(), meta);
        return;
    }

    void putTypeDeclaration(Meta<Attributes> meta) {
        var name = (Named) meta.meta().name();
        environment.putType(name.localName(), meta);
        environment.putType(name.canonicalName(), meta);
        return;
    }

    void putValueDeclaration(Meta<Attributes> meta) {
        var name = (Named) meta.meta().name();
        environment.putValue(name.localName(), meta);
        environment.putValue(name.canonicalName(), meta);
        return;
    }

    <A> A withScope(CodegenScope scope, Supplier<A> fn) {
        environment.pushScope(scope);
        var result = fn.get();
        environment.popScope(scope.getClass());
        return result;
    }

    void withScope(CodegenScope scope, Runnable fn) {
        environment.pushScope(scope);
        fn.run();
        environment.popScope(scope.getClass());
        return;
    }

    public void populateTopLevel(NamespaceNode<Attributes> namespace) {
        namespace.declarations().forEach(decl -> {
            if (decl instanceof LetNode<Attributes> let) {
                putValueDeclaration(let.meta());
            } else if (decl instanceof LetFnNode<Attributes> letFn) {
                putValueDeclaration(letFn.meta());
            } else if (decl instanceof DataNode<Attributes> data) {
                putTypeDeclaration(data.meta());

                data.constructors().forEach(constr -> {
                    putValueDeclaration(constr.meta());

                    constr.params().forEach(constrParam -> {
                        environment.putField(
                                (ConstructorName) constr.meta().meta().name(),
                                constrParam.name(),
                                constrParam.meta());
                    });
                });
            }
        });
    }

    public void generateNamespace(NamespaceNode<Attributes> namespace) {
        var namespaceScope = NamespaceGenScope.open(namespace);

        withScope(namespaceScope, () -> {
            populateTopLevel(namespace);

            namespace.declarations()
                    .forEach(this::generateDeclaration);

            classes.put(
                    Names.getName(namespace),
                    namespaceScope.finaliseNamespace());
        });
    }

    public void generateDeclaration(DeclarationNode<Attributes> declaration) {
        if (declaration instanceof DataNode<Attributes> data) {
            generateData(data);
        } else if (declaration instanceof LetFnNode<Attributes> letFn) {
            generateTopLevelLetFn(letFn);
        } else if (declaration instanceof LetNode<Attributes> let) {
            generateTopLevelLet(let);
        }
    }

    public void generateData(DataNode<Attributes> data) {
        var dataScope = DataGenScope.open(data);
        withScope(dataScope, () -> {
            data.constructors()
                    .forEach(this::generateConstructor);
            classes.put(
                    Names.getName(data),
                    dataScope.finaliseData());
        });
    }

    public void generateConstructor(ConstructorNode<Attributes> constr) {
        var dataScope = environment.enclosingData().get();
        var constrScope = ConstructorGenScope.open(constr, dataScope.data());
        withScope(constrScope, () -> {
            dataScope.classWriter()
                    .visitPermittedSubclass(constrScope.constrType().getInternalName());

            constr.params()
                    .forEachWithIndex(this::generateConstructorParam);

            classes.put(
                    Names.getName(constr),
                    constrScope.finaliseConstructor());
        });
    }

    public void generateConstructorParam(ConstructorParamNode<Attributes> param, int paramIndex) {
        var constructor = environment.enclosingConstructor().get();
        var classWriter = constructor.classWriter();
        var initWriter = constructor.initWriter();

        var paramMinaType = Types.getType(param);
        var paramType = Types.asmType(paramMinaType);
        var paramSignature = JavaSignature.forType(paramMinaType);

        classWriter
                .visitRecordComponent(param.name(), paramType.getDescriptor(), paramSignature)
                .visitEnd();

        Asm.emitConstructorField(
                classWriter,
                initWriter,
                constructor.constrType(),
                paramIndex,
                param.name(),
                paramType,
                paramSignature);

        Asm.emitFieldGetter(
                classWriter,
                constructor.constrType(),
                param.name(),
                paramType,
                JavaSignature.forConstructorInstance(constructor.constr()),
                JavaSignature.forFieldGetter(paramMinaType));
    }

    public void generateTopLevelLetFn(LetFnNode<Attributes> letFn) {
        var namespace = environment.enclosingNamespace().get();
        var namespaceWriter = namespace.classWriter();
        var letScope = TopLevelLetGenScope.open(letFn, namespaceWriter);
        withScope(letScope, () -> {
            generateExpr(letFn.expr());
            letScope.finaliseLet();
        });
    }

    public void generateTopLevelLet(LetNode<Attributes> let) {
        var namespace = environment.enclosingNamespace().get();
        var namespaceWriter = namespace.classWriter();
        var initWriter = namespace.initWriter();

        if (let.expr() instanceof LambdaNode<Attributes> lambda) {
            var letScope = TopLevelLetGenScope.open(let, lambda, namespaceWriter);
            withScope(letScope, () -> {
                generateExpr(lambda.body());
                letScope.finaliseLet();
            });
        } else if (org.mina_lang.common.types.Type.isFunction(Types.getUnderlyingType(let))) {
            // This happens when binding eta-reduced functions.
            // In order to satisfy our binary constraint that top-level functions
            // are static methods, we write a new static method and invoke the returned
            // function object before returning from the method.
            var letScope = TopLevelLetGenScope.open(let, namespaceWriter);

            withScope(letScope, () -> {
                generateExpr(let.expr());

                letScope.methodParams().forEachWithIndex((param, index) -> {
                    letScope.methodWriter().loadArg(index);
                });

                letScope.methodWriter().invokeInterface(
                        Types.asmType(let),
                        Types.erasedMethod("apply", letScope.methodParams().size()));

                var funType = (TypeApply) Types.getUnderlyingType(let);

                var returnType = funType.typeArguments().getLast();

                if (returnType.isPrimitive()) {
                    letScope.methodWriter().unbox(Types.boxedAsmType(returnType));
                } else {
                    letScope.methodWriter().checkCast(Types.boxedAsmType(returnType));
                }

                letScope.finaliseLet();
            });
        } else if (let.expr() instanceof LiteralNode<Attributes> lit) {
            Asm.emitStaticField(namespaceWriter, let.name(), Types.asmType(lit), null, lit.boxedValue());
        } else {
            var fieldType = Types.getType(let);
            var fieldSignature = JavaSignature.forType(fieldType);
            Asm.emitStaticField(namespaceWriter, let.name(), Types.asmType(let), fieldSignature, null);
            var initScope = StaticInitScope.open(let, initWriter, namespaceWriter);
            withScope(initScope, () -> {
                generateExpr(let.expr());
                initScope.finaliseInit();
            });
        }
    }

    public void generateExpr(ExprNode<Attributes> expr) {
        var namespace = environment.enclosingNamespace().get();
        var method = environment.enclosingJavaMethod().get();
        var namespaceWriter = namespace.classWriter();

        if (expr instanceof BlockNode<Attributes> block) {
            var blockScope = BlockGenScope.open(method, block);
            withScope(blockScope, () -> {
                block.declarations().forEach(decl -> {
                    var declName = Names.getName(decl);
                    var localVar = blockScope.localVars().get(declName);
                    generateExpr(decl.expr());
                    method.methodWriter().storeLocal(localVar.index());
                });

                block.result().ifPresentOrElse(result -> {
                    generateExpr(result);
                }, () -> {
                    method.methodWriter().getStatic(
                            Types.asmType(org.mina_lang.common.types.Type.UNIT),
                            "INSTANCE",
                            Types.asmType(org.mina_lang.common.types.Type.UNIT));
                });

                blockScope.finaliseBlock();
            });
        } else if (expr instanceof LambdaNode<Attributes> lambda) {
            var enclosingLifter = environment.enclosingLambdaLifter().get();
            var lambdaScope = LambdaGenScope.open(enclosingLifter, lambda, namespaceWriter);

            withScope(lambdaScope, () -> {
                generateExpr(lambda.body());
                lambdaScope.finaliseLambda();
            });

            var funType = (TypeApply) Types.getUnderlyingType(lambda);

            var lambdaHandle = new Handle(
                    H_INVOKESTATIC,
                    Names.getInternalName(namespace.namespace()),
                    lambdaScope.methodWriter().getName(),
                    Type.getMethodDescriptor(
                            lambdaScope.methodWriter().getReturnType(),
                            lambdaScope.methodWriter().getArgumentTypes()),
                    false);

            var freeVarTypes = lambdaScope
                    .freeVariables()
                    .collect(Types::asmType)
                    .toArray(new Type[lambdaScope.freeVariables().size()]);

            // Stack the free variables of the lambda so they can be captured by the
            // invokedynamic instruction
            lambdaScope.freeVariables().forEach(freeVar -> {
                generateExpr(freeVar);
            });

            method.methodWriter().invokeDynamic(
                    "apply",
                    // Lambda callsite descriptor
                    Type.getMethodDescriptor(Types.asmType(lambda), freeVarTypes),
                    // Bootstrap method handle
                    Asm.METAFACTORY_HANDLE,
                    // Bootstrap method arguments
                    Types.erasedMethodType(funType), // samMethodType
                    lambdaHandle, // implMethodType
                    Types.boxedMethodType(funType) // instantiatedMethodType
            );

        } else if (expr instanceof IfNode<Attributes> ifExpr) {
            var elseLabel = new Label();
            var endLabel = new Label();
            generateExpr(ifExpr.condition());
            method.methodWriter().ifZCmp(GeneratorAdapter.EQ, elseLabel);
            generateExpr(ifExpr.consequent());
            method.methodWriter().goTo(endLabel);
            method.methodWriter().visitLabel(elseLabel);
            generateExpr(ifExpr.alternative());
            method.methodWriter().visitLabel(endLabel);
        } else if (expr instanceof MatchNode<Attributes> match) {
            var matchScope = MatchGenScope.open(method);
            var scrutineeLocal = method.methodWriter()
                    .newLocal(Types.asmType(match.scrutinee()));
            withScope(matchScope, () -> {
                generateExpr(match.scrutinee());
                method.methodWriter().storeLocal(scrutineeLocal);
                match.cases().forEachWithIndex((cse, index) -> generateCase(cse, index, scrutineeLocal));
                matchScope.finaliseMatch();
            });
        } else if (expr instanceof ApplyNode<Attributes> apply) {
            var appliedName = apply.expr().meta().meta().name();
            var applyType = Types.getType(apply);

            var funType = (TypeApply) Types.getUnderlyingType(apply.expr());
            var funReturnType = Types.asmType(funType.typeArguments().getLast());
            var funArgTypes = funType.typeArguments()
                    .take(funType.typeArguments().size() - 1)
                    .collect(Types::asmType)
                    .toArray(new Type[funType.typeArguments().size() - 1]);

            if (appliedName instanceof LetName letName) {
                var namespaceName = letName.name().ns();
                var declarationName = letName.name().name();
                var ownerType = Types.getNamespaceAsmType(namespaceName);

                apply.args()
                        .zip(funType.typeArguments())
                        .forEach(pair -> generateArgExpr(pair.getOne(), pair.getTwo()));

                method.methodWriter().invokeStatic(ownerType, new Method(declarationName, funReturnType, funArgTypes));

                if (!funType.typeArguments().getLast().isPrimitive() && applyType.isPrimitive()) {
                    method.methodWriter().unbox(Types.asmType(applyType));
                } else if (funType.typeArguments().getLast().isPrimitive() && !applyType.isPrimitive()) {
                    method.methodWriter().box(funReturnType);
                } else if (funType.typeArguments().getLast() instanceof TypeVar) {
                    method.methodWriter().checkCast(Types.asmType(applyType));
                }

            } else if (appliedName instanceof ConstructorName constrName) {
                var constrType = Types.getConstructorAsmType(constrName);

                method.methodWriter().newInstance(constrType);
                method.methodWriter().dup();

                apply.args()
                        .zip(funType.typeArguments())
                        .forEach(pair -> generateArgExpr(pair.getOne(), pair.getTwo()));

                method.methodWriter().invokeConstructor(constrType, new Method("<init>", Type.VOID_TYPE, funArgTypes));
                // Not exactly necessary - this is here to upcast constructors into their parent
                // data type.
                // It was added to avoid a case in the ASM verifier which requires the generated
                // classes to be loaded into the VM during verification (common supertype
                // checks).
                method.methodWriter().checkCast(Types.getDataAsmType(constrName.enclosing()));

            } else if (appliedName.equals(Nameless.INSTANCE) || appliedName instanceof LocalName) {
                generateExpr(apply.expr());

                apply.args()
                        .zip(funType.typeArguments())
                        .forEach(pair -> generateArgExpr(pair.getOne(), pair.getTwo()));

                method.methodWriter().invokeInterface(
                        Types.asmType(funType),
                        Types.erasedMethod("apply", funArgTypes.length));

                if (!funType.typeArguments().getLast().isPrimitive() && applyType.isPrimitive()) {
                    method.methodWriter().unbox(Types.asmType(applyType));
                } else if (funType.typeArguments().getLast().isPrimitive() && !applyType.isPrimitive()) {
                    method.methodWriter().box(funReturnType);
                } else if (funType.typeArguments().getLast() instanceof TypeVar) {
                    method.methodWriter().checkCast(Types.asmType(applyType));
                }
            }

        } else if (expr instanceof LiteralNode<Attributes> lit) {
            generateLiteral(lit);
        } else if (expr instanceof ReferenceNode<Attributes> ref) {
            var name = Names.getName(ref);
            var type = Types.getUnderlyingType(ref);

            if (name instanceof LetName let && org.mina_lang.common.types.Type.isFunction(type)) {
                var funType = (TypeApply) type;
                var letHandle = Asm.staticMethodHandle(let, type);
                method.methodWriter().invokeDynamic(
                        // Interface method name
                        "apply",
                        // Lambda callsite descriptor
                        Type.getMethodDescriptor(Types.asmType(ref)),
                        // Bootstrap method handle
                        Asm.METAFACTORY_HANDLE,
                        // Bootstrap method arguments
                        Types.erasedMethodType(funType), // samMethodType
                        letHandle, // implMethodType
                        Types.boxedMethodType(funType) // instantiatedMethodType
                );
            } else if (name instanceof ConstructorName constr) {
                var funType = (TypeApply) type;
                var constrHandle = Asm.constructorMethodHandle(constr, type);
                method.methodWriter().invokeDynamic(
                        // Interface method name
                        "apply",
                        // Lambda callsite descriptor
                        Type.getMethodDescriptor(Types.asmType(ref)),
                        // Bootstrap method handle
                        Asm.METAFACTORY_HANDLE,
                        // Bootstrap method arguments
                        Types.erasedMethodType(funType), // samMethodType
                        constrHandle, // implMethodType
                        Types.boxedMethodType(funType) // instantiatedMethodType
                );
            } else if (name instanceof LetName let) {
                method.methodWriter().getStatic(
                        Types.getNamespaceAsmType(let.name().ns()),
                        let.name().name(),
                        Types.asmType(ref));
            } else if (name instanceof LocalName local) {
                environment.lookupLocalVar(local).ifPresentOrElse(localVar -> {
                    method.methodWriter().loadLocal(localVar.index());
                }, () -> {
                    var localVar = method.methodParams().get(local);
                    method.methodWriter().loadArg(localVar.index());
                });
            }
        }
    }

    public void generateArgExpr(ExprNode<Attributes> argExpr, org.mina_lang.common.types.Type funArgType) {
        var method = environment.enclosingJavaMethod().get();

        var appliedArgType = Types.getType(argExpr);

        var appliedAsmType = Types.asmType(appliedArgType);
        var funArgAsmType = Types.asmType(funArgType);

        generateExpr(argExpr);

        if (!funArgType.isPrimitive() && appliedArgType.isPrimitive()) {
            method.methodWriter().box(appliedAsmType);
        } else if (funArgType.isPrimitive() && !appliedArgType.isPrimitive()) {
            method.methodWriter().unbox(funArgAsmType);
        }
    }

    public void generateLiteral(LiteralNode<Attributes> literal) {
        var method = environment.enclosingJavaMethod().get();

        if (literal instanceof BooleanNode<Attributes> bool) {
            method.methodWriter().push(bool.value());
        } else if (literal instanceof CharNode<Attributes> chr) {
            method.methodWriter().push(chr.value());
        } else if (literal instanceof StringNode<Attributes> str) {
            method.methodWriter().push(str.value());
        } else if (literal instanceof IntNode<Attributes> intgr) {
            method.methodWriter().push(intgr.value());
        } else if (literal instanceof LongNode<Attributes> lng) {
            method.methodWriter().push(lng.value());
        } else if (literal instanceof FloatNode<Attributes> flt) {
            method.methodWriter().push(flt.value());
        } else if (literal instanceof DoubleNode<Attributes> dbl) {
            method.methodWriter().push(dbl.value());
        }
    }

    public void generateCase(CaseNode<Attributes> caseNode, int caseIndex, int scrutineeLocal) {
        var method = environment.enclosingJavaMethod().get();
        var match = environment.enclosingMatch().get();
        var caseScope = CaseGenScope.open(method, match);

        withScope(caseScope, () -> {
            generatePattern(caseNode.pattern(), scrutineeLocal);
            generateExpr(caseNode.consequent());
            caseScope.finaliseCase();
        });
    }

    public void generatePattern(PatternNode<Attributes> pattern, int scrutineeLocal) {
        var method = environment.enclosingJavaMethod().get();
        var caseScope = environment.enclosingCase().get();

        method.methodWriter().loadLocal(scrutineeLocal);

        if (pattern instanceof IdPatternNode<Attributes> idPat) {
            var idPatVar = caseScope.putLocalVar(idPat);
            method.methodWriter().storeLocal(idPatVar);

        } else if (pattern instanceof AliasPatternNode<Attributes> aliasPat) {
            var aliasPatVar = caseScope.putLocalVar(aliasPat);
            method.methodWriter().storeLocal(aliasPatVar);

            generatePattern(aliasPat.pattern(), aliasPatVar);

        } else if (pattern instanceof LiteralPatternNode<Attributes> litPat) {
            generateLiteral(litPat.literal());

            if (litPat.literal() instanceof StringNode<Attributes> strPat) {
                var equalsDescriptor = Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Types.OBJECT_TYPE);
                var equalsMethod = new Method("equals", equalsDescriptor);

                method.methodWriter().invokeVirtual(Types.STRING_TYPE, equalsMethod);
                method.methodWriter().ifZCmp(GeneratorAdapter.EQ, caseScope.endLabel());
            } else {
                method.methodWriter().ifCmp(Types.asmType(litPat), GeneratorAdapter.NE, caseScope.endLabel());
            }
        } else if (pattern instanceof ConstructorPatternNode<Attributes> constrPat) {
            var constrMeta = environment.lookupValue(constrPat.id().canonicalName()).get();
            var constrName = (ConstructorName) constrMeta.meta().name();
            var constrType = Types.getConstructorAsmType(constrName);

            method.methodWriter().instanceOf(constrType);
            method.methodWriter().ifZCmp(GeneratorAdapter.EQ, caseScope.endLabel());

            constrPat.fields().forEach(fieldPat -> {
                var fieldMeta = environment.lookupField(constrName, fieldPat.field()).get();
                var fieldMinaType = (org.mina_lang.common.types.Type) fieldMeta.meta().sort();
                var fieldType = Types.asmType(fieldMinaType);

                var patMinaType = Types.getType(fieldPat);
                var patType = Types.asmType(patMinaType);

                var getterDescriptor = Type.getMethodDescriptor(fieldType);
                var getterMethod = new Method(fieldPat.field(), getterDescriptor);

                var nestedPatLocal = fieldPat.pattern()
                    .map(nestedPat -> method.methodWriter().newLocal(patType))
                    .orElseGet(() -> caseScope.putLocalVar(fieldPat));

                method.methodWriter().loadLocal(scrutineeLocal);
                method.methodWriter().checkCast(constrType);
                method.methodWriter().invokeVirtual(constrType, getterMethod);

                if (fieldMinaType.isPrimitive() && !patMinaType.isPrimitive()) {
                    method.methodWriter().box(fieldType);
                } else if (!fieldMinaType.isPrimitive() && patMinaType.isPrimitive()) {
                    method.methodWriter().unbox(patType);
                } else if (fieldMinaType instanceof TypeVar tyVar) {
                    method.methodWriter().checkCast(patType);
                }

                method.methodWriter().storeLocal(nestedPatLocal);

                fieldPat.pattern().ifPresent(nestedPattern -> {
                    generatePattern(nestedPattern, nestedPatLocal);
                });
            });
        }

    }
}
