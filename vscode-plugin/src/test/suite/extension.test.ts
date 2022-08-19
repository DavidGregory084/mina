import * as assert from "assert";
import * as vscode from "vscode";

suite("Mina Language Extension", () => {
  test("should be present", () => {
    assert.ok(vscode.extensions.getExtension("mina-lang.mina-lang-vscode"));
  });

  test("should activate successfully", async () => {
    await vscode.extensions
      .getExtension("mina-lang.mina-lang-vscode")
      .activate();

    return assert.ok(true);
  }).timeout(60000);
});
