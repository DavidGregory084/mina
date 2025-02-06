/*
 * SPDX-FileCopyrightText:  © 2022-2024 David Gregory
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mina_lang.codegen.jvm;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.Maps;
import org.mina_lang.codegen.jvm.scopes.*;
import org.mina_lang.common.Attributes;
import org.mina_lang.common.Meta;
import org.mina_lang.common.Scope;
import org.mina_lang.common.names.*;
import org.mina_lang.common.types.Sort;
import org.mina_lang.common.types.TypeApply;
import org.mina_lang.proto.ProtobufWriter;
import org.mina_lang.syntax.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.mina_lang.syntax.SyntaxNodes.*;
import static org.objectweb.asm.Opcodes.H_INVOKESTATIC;

public class CodeGenerator {
    CodegenEnvironment environment = CodegenEnvironment.empty();

    ProtobufWriter protobufWriter = new ProtobufWriter();

    MutableMap<Named, byte[]> classes = Maps.mutable.empty();

    public void generate(Path destination, NamespaceNode<Attributes> namespace) throws IOException {
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

    <A extends CodegenScope, B> B withScope(A scope, Function<A, B> fn) {
        environment.pushScope(scope);
        var result = fn.apply(scope);
        environment.popScope(scope.getClass());
        return result;
    }

    <A extends CodegenScope> void withScope(A scope, Consumer<A> fn) {
        environment.pushScope(scope);
        fn.accept(scope);
        environment.popScope(scope.getClass());
        return;
    }

    public void populateTopLevel(NamespaceNode<Attributes> namespace) {
        namespace.declarationGroups().forEach(decls -> {
            decls.forEach(decl -> {
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
        });
    }

    public void generateNamespace(NamespaceNode<Attributes> namespace) {
        withScope(NamespaceGenScope.open(namespace, protobufWriter), namespaceScope -> {
            populateTopLevel(namespace);

            namespace.declarationGroups()
                    .forEach(decls -> decls.forEach(this::generateDeclaration));

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
        withScope(DataGenScope.open(data), dataScope -> {
            data.constructors()
                    .forEach(this::generateConstructor);
            classes.put(
                    Names.getName(data),
                    dataScope.finaliseData());
        });
    }

    public void generateConstructor(ConstructorNode<Attributes> constr) {
        var dataScope = environment.enclosingData().get();

        withScope(ConstructorGenScope.open(constr, dataScope.data()), constrScope -> {
            dataScope.classWriter()
                    .visitPermittedSubclass(constrScope.constrType().getInternalName());

            constr.params()
                    .forEachWithIndex(this::generateConstructorParam);

            Asm.emitObjectBootstrapMethod(
                    "equals",
                    Type.BOOLEAN_TYPE,
                    Lists.immutable.of(Types.OBJECT_TYPE),
                    constrScope.classWriter(),
                    constrScope.constrType(),
                    JavaSignature.forConstructorInstance(constr),
                    null,
                    constr.params());

            Asm.emitObjectBootstrapMethod(
                    "hashCode",
                    Type.INT_TYPE,
                    Lists.immutable.empty(),
                    constrScope.classWriter(),
                    constrScope.constrType(),
                    JavaSignature.forConstructorInstance(constr),
                    null,
                    constr.params());

            Asm.emitObjectBootstrapMethod(
                    "toString",
                    Types.STRING_TYPE,
                    Lists.immutable.empty(),
                    constrScope.classWriter(),
                    constrScope.constrType(),
                    JavaSignature.forConstructorInstance(constr),
                    null,
                    constr.params());

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
        withScope(TopLevelLetGenScope.open(letFn, namespaceWriter), letScope -> {
            generateExpr(letFn.expr());
            letScope.finaliseLet();
        });
    }

    public void generateTopLevelLet(LetNode<Attributes> let) {
        var namespace = environment.enclosingNamespace().get();
        var namespaceWriter = namespace.classWriter();
        var initWriter = namespace.initWriter();

        if (let.expr() instanceof LambdaNode<Attributes> lambda) {
            withScope(TopLevelLetGenScope.open(let, lambda, namespaceWriter), letScope -> {
                generateExpr(lambda.body());
                letScope.finaliseLet();
            });
        } else if (org.mina_lang.common.types.Type.isFunction(Types.getUnderlyingType(let))) {
            // This happens when binding eta-reduced functions.
            // In order to satisfy our binary constraint that top-level functions
            // are static methods, we write a new static method and invoke the returned
            // function object before returning from the method.

            withScope(TopLevelLetGenScope.open(let, namespaceWriter), letScope -> {
                generateExpr(let.expr());

                var funType = (TypeApply) Types.getUnderlyingType(let);

                letScope.methodParams()
                    .toSortedListBy(LocalVar::index)
                    .forEach(param -> {
                        letScope.methodWriter().loadArg(param.index());
                        var argType = funType.typeArguments().get(param.index());
                        if (argType.isPrimitive()) {
                            letScope.methodWriter().box(Types.asmType(argType));
                        }
                    });

                letScope.methodWriter().invokeInterface(
                        Types.asmType(let),
                        Types.erasedMethod("apply", letScope.methodParams().size()));

                Asm.unboxReturnValue(letScope.methodWriter(), funType.typeArguments().getLast());

                letScope.finaliseLet();
            });
        } else if (let.expr() instanceof LiteralNode<Attributes> lit) {
            Asm.emitStaticField(namespaceWriter, let.name(), Types.asmType(lit), null, lit.boxedValue());
        } else {
            var fieldType = Types.getType(let);
            var fieldSignature = JavaSignature.forType(fieldType);
            Asm.emitStaticField(namespaceWriter, let.name(), Types.asmType(let), fieldSignature, null);
            withScope(StaticInitScope.open(let, initWriter, namespaceWriter), initScope -> {
                generateExpr(let.expr());
                initScope.finaliseInit();
            });
        }
    }

    private int compareOpcodeFor(BinaryOp binaryOp) {
        if (binaryOp == null) {
            return GeneratorAdapter.NE;
        } else {
            return switch (binaryOp) {
                case LESS_THAN -> GeneratorAdapter.GE;
                case LESS_THAN_EQUAL -> GeneratorAdapter.GT;
                case GREATER_THAN -> GeneratorAdapter.LE;
                case GREATER_THAN_EQUAL -> GeneratorAdapter.LT;
                case EQUAL -> GeneratorAdapter.NE;
                default -> GeneratorAdapter.EQ;
            };
        }
    }

    private int invertedCompareOpcodeFor(BinaryOp binaryOp) {
        if (binaryOp == null) {
            return GeneratorAdapter.EQ;
        } else {
            return switch (binaryOp) {
                case LESS_THAN -> GeneratorAdapter.LT;
                case LESS_THAN_EQUAL -> GeneratorAdapter.LE;
                case GREATER_THAN -> GeneratorAdapter.GT;
                case GREATER_THAN_EQUAL -> GeneratorAdapter.GE;
                case EQUAL -> GeneratorAdapter.EQ;
                default -> GeneratorAdapter.NE;
            };
        }
    }

    private int opcodeFor(BinaryOp binaryOp) {
        return switch (binaryOp) {
            case MULTIPLY -> GeneratorAdapter.MUL;
            case DIVIDE -> GeneratorAdapter.DIV;
            case MODULUS -> GeneratorAdapter.REM;
            case ADD -> GeneratorAdapter.ADD;
            case SUBTRACT -> GeneratorAdapter.SUB;
            case SHIFT_LEFT -> GeneratorAdapter.SHL;
            case SHIFT_RIGHT -> GeneratorAdapter.SHR;
            case UNSIGNED_SHIFT_RIGHT -> GeneratorAdapter.USHR;
            case BITWISE_AND -> GeneratorAdapter.AND;
            case BITWISE_OR -> GeneratorAdapter.OR;
            case BITWISE_XOR -> GeneratorAdapter.XOR;
            default -> throw new IllegalStateException("Unexpected binary operator used in arithmetic operation: " + binaryOp);
        };
    }

    public void generateExpr(ExprNode<Attributes> expr) {
        var namespace = environment.enclosingNamespace().get();
        var method = environment.enclosingJavaMethod().get();
        var namespaceWriter = namespace.classWriter();

        if (expr instanceof BlockNode<Attributes> block) {
            withScope(BlockGenScope.open(method, block), blockScope -> {
                block.declarations().forEach(decl -> {
                    var declName = Names.getName(decl);
                    var localVar = blockScope.localVars().get(declName);
                    generateExpr(decl.expr());
                    method.methodWriter().storeLocal(localVar.index());
                });

                block.result().ifPresentOrElse(this::generateExpr, () -> {
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

            withScope(lambdaScope, scope -> {
                generateExpr(lambda.body());
                scope.finaliseLambda();
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
            lambdaScope.freeVariables().forEach(this::generateExpr);

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

        } else if (expr instanceof SelectNode<Attributes> select) {
            // This case handles a selection that is not immediately applied.
            // Code generation for ApplyNode handles the other case.
            var selectMeta = select.meta();

            // Create a lambda to represent the partial application of the selection
            // receiver.selection ==> (..restArgs) => selection(receiver, ..restArgs)
            // TODO: Investigate using a bootstrap method with MethodHandle#bind to simplify this
            var funType = (TypeApply) Types.getUnderlyingType(select);

            var lambdaAttrs = Attributes.nameless(selectMeta.meta().sort());
            var lambdaMeta = selectMeta.withMeta(lambdaAttrs);

            var lambdaParams = funType.typeArguments()
                .take(funType.typeArguments().size() - 1)
                .collectWithIndex((paramTy, index) -> {
                    var paramName = new LocalName("arg" +  index, 0);
                    return paramNode(Meta.of(paramName, paramTy), "arg" + index);
                });

            var applyAttrs = Attributes.nameless(funType.typeArguments().getLast());
            var applyMeta = selectMeta.withMeta(applyAttrs);

            var appliedExprs = Lists.immutable.of(select.receiver())
                .newWithAll(funType.typeArguments()
                    .take(funType.typeArguments().size() - 1)
                    .collectWithIndex((paramTy, index) -> {
                        var paramName = new LocalName("arg" +  index, 0);
                        return refNode(Meta.of(paramName, paramTy), "arg" + index);
                    }));

            var lambda = lambdaNode(
                lambdaMeta,
                lambdaParams,
                applyNode(applyMeta, select.selection(), appliedExprs));

            generateExpr(lambda);

        } else if (expr instanceof IfNode<Attributes> ifExpr) {
            withScope(IfGenScope.open(), ifScope -> {
                generateBooleanExpr(ifExpr.condition(), ifScope.thenLabel(), ifScope.elseLabel(), ifScope.thenLabel());
                method.methodWriter().visitLabel(ifScope.thenLabel());
                generateExpr(ifExpr.consequent());
                method.methodWriter().goTo(ifScope.endLabel());
                method.methodWriter().visitLabel(ifScope.elseLabel());
                generateExpr(ifExpr.alternative());
                method.methodWriter().visitLabel(ifScope.endLabel());
            });
        } else if (expr instanceof MatchNode<Attributes> match) {
            withScope(MatchGenScope.open(method), matchScope -> {
                var scrutineeType = Types.asmType(match.scrutinee());
                var scrutineeLocal = method.methodWriter().newLocal(scrutineeType);
                generateExpr(match.scrutinee());
                method.methodWriter().storeLocal(scrutineeLocal);
                match.cases().forEachWithIndex((cse, index) -> generateCase(cse, index, scrutineeLocal));
                matchScope.finaliseMatch();
            });
        } else if (expr instanceof ApplyNode<Attributes> apply) {
            var applyType = Types.getType(apply);

            ExprNode<Attributes> appliedExpr;
            ImmutableList<ExprNode<Attributes>> appliedArgs;

            if (apply.expr() instanceof SelectNode<Attributes> select) {
                appliedExpr = select.selection();
                appliedArgs = Lists.immutable.of(select.receiver()).newWithAll(apply.args());
            } else {
                appliedExpr = apply.expr();
                appliedArgs = apply.args();
            }

            Name appliedName = appliedExpr.meta().meta().name();
            TypeApply appliedType = (TypeApply) Types.getUnderlyingType(appliedExpr);

            var funReturnType = Types.asmType(appliedType.typeArguments().getLast());
            var funArgTypes = appliedType.typeArguments()
                    .take(appliedType.typeArguments().size() - 1)
                    .collect(Types::asmType)
                    .toArray(new Type[appliedType.typeArguments().size() - 1]);

            if (appliedName instanceof LetName letName) {
                var namespaceName = letName.name().ns();
                var declarationName = letName.name().name();
                var ownerType = Types.getNamespaceAsmType(namespaceName);

                appliedArgs
                        .zip(appliedType.typeArguments())
                        .forEach(pair -> generateArgExpr(pair.getOne(), pair.getTwo()));

                method.methodWriter().invokeStatic(ownerType, new Method(declarationName, funReturnType, funArgTypes));

                Asm.boxUnboxReturnValue(method.methodWriter(), appliedType.typeArguments().getLast(), applyType);

            } else if (appliedName instanceof ConstructorName constrName) {
                var constrType = Types.getConstructorAsmType(constrName);

                method.methodWriter().newInstance(constrType);
                method.methodWriter().dup();

                appliedArgs
                        .zip(appliedType.typeArguments())
                        .forEach(pair -> generateArgExpr(pair.getOne(), pair.getTwo()));

                method.methodWriter().invokeConstructor(constrType, new Method("<init>", Type.VOID_TYPE, funArgTypes));
                // Not exactly necessary - this is here to upcast constructors into their parent
                // data type.
                // It was added to avoid a case in the ASM verifier which requires the generated
                // classes to be loaded into the VM during verification (common supertype
                // checks).
                method.methodWriter().checkCast(Types.getDataAsmType(constrName.enclosing()));

            } else if (appliedName.equals(Nameless.INSTANCE) || appliedName instanceof LocalName) {
                generateExpr(appliedExpr);

                appliedArgs
                        .zip(appliedType.typeArguments())
                        .forEach(pair -> generateBoxedArgExpr(pair.getOne(), pair.getTwo()));

                method.methodWriter().invokeInterface(
                        Types.asmType(appliedType),
                        Types.erasedMethod("apply", funArgTypes.length));

                Asm.unboxReturnValue(method.methodWriter(), applyType);
            }
        } else if (expr instanceof UnaryOpNode<Attributes> unaryOp) {
            var operandType = Types.asmType(unaryOp.operand());

            switch (unaryOp.operator()) {
                case NEGATE -> {
                    generateExpr(unaryOp.operand());
                    method.methodWriter().math(GeneratorAdapter.NEG, operandType);
                }
                case BOOLEAN_NOT -> {
                    withScope(IfGenScope.open(), ifScope -> {
                        generateBooleanExpr(unaryOp.operand(), ifScope.elseLabel(), ifScope.thenLabel(), ifScope.thenLabel());
                        method.methodWriter().visitLabel(ifScope.thenLabel());
                        method.methodWriter().push(true);
                        method.methodWriter().goTo(ifScope.endLabel());
                        method.methodWriter().visitLabel(ifScope.elseLabel());
                        method.methodWriter().push(false);
                        method.methodWriter().visitLabel(ifScope.endLabel());
                    });
                }
                case BITWISE_NOT -> {
                    generateExpr(unaryOp.operand());

                    if (operandType.getSort() == Type.LONG) {
                        method.methodWriter().visitInsn(Opcodes.LCONST_1);
                    } else {
                        method.methodWriter().visitInsn(Opcodes.ICONST_1);
                    }

                    method.methodWriter().math(GeneratorAdapter.XOR, operandType);
                }
            }

        } else if (expr instanceof BinaryOpNode<Attributes> binaryOp) {
            var operator = binaryOp.operator();
            var leftOperandType = Types.asmType(binaryOp.leftOperand());

            if (BinaryOp.BOOLEAN_OPERATORS.contains(operator)) {
                withScope(IfGenScope.open(), ifScope -> {
                    generateBooleanOp(binaryOp, ifScope.thenLabel(), ifScope.elseLabel(), ifScope.thenLabel());
                    method.methodWriter().visitLabel(ifScope.thenLabel());
                    method.methodWriter().push(true);
                    method.methodWriter().goTo(ifScope.endLabel());
                    method.methodWriter().visitLabel(ifScope.elseLabel());
                    method.methodWriter().push(false);
                    method.methodWriter().visitLabel(ifScope.endLabel());
                });
            } else {
                generateExpr(binaryOp.leftOperand());
                generateExpr(binaryOp.rightOperand());
                method.methodWriter().math(opcodeFor(operator), leftOperandType);
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
                environment.lookupLocalVarIn(method.methodWriter(), local).ifPresentOrElse(localVar -> {
                    method.methodWriter().loadLocal(localVar.index());
                }, () -> {
                    var methodParam = Optional.ofNullable(method.methodParams().get(local));
                    methodParam.ifPresentOrElse(localVar -> {
                        method.methodWriter().loadArg(localVar.index());
                    }, () -> {
                        throw new IllegalStateException("Unable to look up local variable " + local.name() + "@" + local.index());
                    });
                });
            }
        }
    }

    public void generateArgExpr(ExprNode<Attributes> argExpr, org.mina_lang.common.types.Type funArgType) {
        var method = environment.enclosingJavaMethod().get();
        var appliedArgType = Types.getType(argExpr);
        generateExpr(argExpr);
        Asm.boxUnboxArgExpr(method.methodWriter(), funArgType, appliedArgType);
    }

    public void generateBoxedArgExpr(ExprNode<Attributes> argExpr, org.mina_lang.common.types.Type funArgType) {
        var method = environment.enclosingJavaMethod().get();
        var appliedArgType = Types.getType(argExpr);
        generateExpr(argExpr);
        method.methodWriter().box(Types.asmType(appliedArgType));
    }

    public void generateComparison(ExprNode<Attributes> expr, Set<BinaryOp> operators, BinaryOp enclosingOp, Type leftOperandType, Label trueLabel, Label falseLabel, Label nextOpLabel) {
        var method = environment.enclosingJavaMethod().get();

        // Branch to the false label by default, unless it is already the next operation
        var branchLabel = !nextOpLabel.equals(falseLabel) ? falseLabel : trueLabel;

        // Are we branching to the false label?
        var branchingToFalse = branchLabel == falseLabel;

        // Fall through to the true label by default, unless we had to invert our branch label
        var alternativeLabel = branchingToFalse ? trueLabel : falseLabel;

        var comparisonOpcode = branchingToFalse
            ? compareOpcodeFor(enclosingOp)
            : invertedCompareOpcodeFor(enclosingOp);

        var invertedComparisonOpcode = branchingToFalse
            ? invertedCompareOpcodeFor(enclosingOp)
            : compareOpcodeFor(enclosingOp);

        if (enclosingOp == null) {
            // It's a boolean value without any enclosing operators:
            // We're comparing to zero, so we must invert our comparison
            method.methodWriter().ifZCmp(invertedComparisonOpcode, branchLabel);
            if (!nextOpLabel.equals(alternativeLabel)) method.methodWriter().goTo(alternativeLabel);

        } else if (operators.contains(enclosingOp)) {

            if (BinaryOp.RELATIONAL_OPERATORS.contains(enclosingOp)) {
                method.methodWriter().ifCmp(Types.asmType(expr), comparisonOpcode, branchLabel);
            } else if (BinaryOp.EQUALITY_OPERATORS.contains(enclosingOp)) {

                if (leftOperandType.getSort() == Type.OBJECT) {
                    // We have a reference type, so call .equals
                    method.methodWriter().invokeVirtual(leftOperandType, Asm.EQUALS_METHOD);
                    // We're comparing to zero, so we must invert our comparison
                    method.methodWriter().ifZCmp(invertedComparisonOpcode, branchLabel);
                } else {
                    method.methodWriter().ifCmp(leftOperandType, comparisonOpcode, branchLabel);
                }
            } else {
                method.methodWriter().ifZCmp(comparisonOpcode, branchLabel);
            }

            // Jump to the alternative if we didn't branch, unless it's the next operation anyway
            if (!nextOpLabel.equals(alternativeLabel)) method.methodWriter().goTo(alternativeLabel);
        }
    }

    public void generateBooleanExpr(ExprNode<Attributes> expr, Label trueLabel, Label falseLabel, Label nextOpLabel) {
        generateBooleanExpr(expr, BinaryOp.VALUES, null, null, trueLabel, falseLabel, nextOpLabel);
    }

    public void generateBooleanExpr(ExprNode<Attributes> expr, Set<BinaryOp> operators, BinaryOp enclosingOp, Type enclosingType, Label trueLabel, Label falseLabel, Label nextOpLabel) {
        if (
            // We cannot translate the children of comparison operators into jumps, because they need operands to compare!
            (enclosingOp == null || BinaryOp.LOGICAL_OPERATORS.contains(enclosingOp)) &&
            expr instanceof BinaryOpNode<Attributes> binOp &&
            BinaryOp.BOOLEAN_OPERATORS.contains(binOp.operator())
        ) {
            generateBooleanOp(binOp, trueLabel, falseLabel, nextOpLabel);
        } else if (
            expr instanceof UnaryOpNode<Attributes> unOp &&
            unOp.operator().equals(UnaryOp.BOOLEAN_NOT)
        ) {
            // The ! operator swaps the true and false jump targets
            generateBooleanExpr(unOp.operand(), operators, enclosingOp, enclosingType, falseLabel, trueLabel, nextOpLabel);
        } else {
            generateExpr(expr);
            generateComparison(expr, operators, enclosingOp, enclosingType, trueLabel, falseLabel, nextOpLabel);
        }
    }

    public void generateBooleanOp(BinaryOpNode<Attributes> booleanOp, Label trueLabel, Label falseLabel, Label nextOpLabel) {
        var method = environment.enclosingJavaMethod().get();

        var leftOperandType = Types.asmType(booleanOp.leftOperand());
        var operator = booleanOp.operator();

        var breakLabel = new Label();

        generateBooleanExpr(
            booleanOp.leftOperand(),
            // Generate comparisons only for logical operators, as they can short-circuit
            BinaryOp.LOGICAL_OPERATORS,
            operator,
            leftOperandType,
            // Nested || operators jump to the right && operand when short-circuiting
            operator.equals(BinaryOp.BOOLEAN_AND) ? breakLabel : trueLabel,
            // Nested && operators jump to the right || operand when short-circuiting
            operator.equals(BinaryOp.BOOLEAN_OR) ? breakLabel : falseLabel,
            // The next operand is the label we will fall through to by default
            breakLabel
        );

        method.methodWriter().visitLabel(breakLabel);

        generateBooleanExpr(
            booleanOp.rightOperand(),
            // Generate comparisons for all operators
            BinaryOp.VALUES,
            operator,
            leftOperandType,
            // Nested operators jump to the enclosing true state when short-circuiting
            trueLabel,
            // Nested operators jump to the enclosing false state when short-circuiting
            falseLabel,
            // The next state is the label we will fall through to by default
            nextOpLabel
        );
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
        withScope(CaseGenScope.open(method, match), caseScope -> {
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
                method.methodWriter().invokeVirtual(Types.STRING_TYPE, Asm.EQUALS_METHOD);
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

                Asm.boxUnboxReturnValue(method.methodWriter(), fieldMinaType, patMinaType);

                method.methodWriter().storeLocal(nestedPatLocal);

                fieldPat.pattern().ifPresent(nestedPattern -> {
                    generatePattern(nestedPattern, nestedPatLocal);
                });
            });
        }

    }
}
