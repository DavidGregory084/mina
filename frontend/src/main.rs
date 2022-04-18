use std::io::{self, BufRead, Result, Write};

use lrlex::lrlex_mod;
use lrpar::Lexer;
// use lrpar::lrpar_mod;

lrlex_mod!("mina.l");
// lrpar_mod!("mina.y");

fn main() -> Result<()> {
    let lexerdef = mina_l::lexerdef();
    let stdin = std::io::stdin();

    loop {
        io::stdout().flush()?;

        match stdin.lock().lines().next() {
            Some(Ok(ref l)) => {
                if l.trim().is_empty() {
                    continue;
                }

                let lexer = lexerdef.lexer(l);

                lexer.iter().for_each(|result| match result {
                    Ok(lexeme) => println!("{:?}", lexeme),
                    Err(err) => println!("{:?}", err),
                });
            }
            _ => break,
        }
    }

    Ok(())
}
