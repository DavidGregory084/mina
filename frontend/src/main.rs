use std::io::{self, BufRead, Result, Write};

use lrlex::lrlex_mod;
use lrpar::lrpar_mod;

lrlex_mod!("mina.l");
lrpar_mod!("mina.y");

fn main() -> Result<()> {
    let lexerdef = mina_l::lexerdef();
    let stdin = std::io::stdin();

    loop {
        io::stdout().flush()?;

        match stdin.lock().lines().next() {
            Some(Ok(ref line)) => {
                if line.trim().is_empty() {
                    continue;
                }

                let lexer = lexerdef.lexer(line);
                let errs = mina_y::parse(&lexer);

                for err in errs {
                    println!("{}", err.pp(&lexer, &mina_y::token_epp));
                }
            }
            _ => break,
        }
    }

    Ok(())
}
