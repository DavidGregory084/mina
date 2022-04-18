use cfgrammar::yacc::YaccKind;
use lrlex::CTLexerBuilder;

fn main() -> Result<(), Box<dyn std::error::Error>> {
    CTLexerBuilder::new()
        // .lrpar_config(|ctp| {
        //     ctp.yacckind(YaccKind::Grmtools)
        //         .grammar_in_src_dir("mina.y")
        //         .unwrap()
        // })
        .lexer_in_src_dir("mina.l")?
        .build()?;

    prost_build::compile_protos(
        &["src/ast.proto"],
        &["src"]
    )?;

    Ok(())
}
