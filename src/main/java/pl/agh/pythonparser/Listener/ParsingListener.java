package pl.agh.pythonparser.Listener;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.codehaus.plexus.util.StringUtils;
import pl.agh.io.FileAccessor;
import pl.agh.pythonparser.Mapper.Dictionary;
import pl.agh.pythonparser.Mapper.Mapper;
import pl.agh.pythonparser.Python3BaseListener;
import pl.agh.pythonparser.Python3Parser;

import java.util.ArrayList;

import static pl.agh.pythonparser.Mapper.Mapper.*;

public class ParsingListener extends Python3BaseListener {

    private String filename;
    private String content;
    private int depth;
    public ArrayList<String> variables;
    private String pythonInfo;

    public ParsingListener() {
        this("result");
    }

    public ParsingListener(String filename) {
        this.filename = filename;
        this.pythonInfo = "";
        this.variables = new ArrayList<>();
        this.depth = 1;
    }


    @Override
    public void exitSingle_input(Python3Parser.Single_inputContext ctx) {

    }

    @Override
    public void enterFile_input(Python3Parser.File_inputContext ctx) {
        this.content = Dictionary.MAIN_CLASS_INTRO;
        makeIndication();
        openSourceClass();
    }

    @Override
    public void exitFile_input(Python3Parser.File_inputContext ctx) {

        closeSourceClass();

        //TODO popraw to bo to tak nie moze byc
        makeIndication(1);
        this.content = this.content.concat(
                Dictionary.MAIN_FUNCTION_INTRO
                        + Dictionary.MAIN_FUNCTION_BODY
                        + Dictionary.MAIN_FUNCTION_OUTRO
        );

        this.content = this.content.concat(Dictionary.CLOSE_BRACE);

        this.content = this.content.replaceAll("'","\"");

        boolean saved = new FileAccessor().save(
                this.content,
                this.filename
        );

        if (saved) {
            System.out.println("Saved in " + this.filename );
        } else {
            System.out.println("Cannot save...");
        }
    }

    @Override
    public void enterFuncdef(@NotNull Python3Parser.FuncdefContext ctx) {
        makeIndication();

        if (Mapper.isSpecial(ctx.getChild(0).getText())) {
            this.content = this.content.concat("public ");
            this.content = this.content.concat(Mapper.getSpecial(ctx.getChild(0).getText()));
            this.content = this.content.concat(Dictionary.SPACE);
            this.content = this.content.concat(ctx.getChild(1).getText());
        }
    }

    @Override
    public void exitFuncdef(Python3Parser.FuncdefContext ctx) {
        makeIndication();
        this.content = this.content.concat( "}" );

        if (hasExtendedStatement(ctx, Python3Parser.Return_stmtContext.class)) {
            this.content=cutLastOccurence(
                    this.content,
                    "public function",
                    "public "
                            +
                            getFunctionType(
                                    getExtendedChild(ctx, Python3Parser.Return_stmtContext.class),
                                    this.variables
            ));
        }  else {
            this.content=cutLastOccurence(this.content,"public function","public void");
        }
    }

    @Override
    public void enterParameters(Python3Parser.ParametersContext ctx) {
        this.content = this.content.concat( "(" );
    }

    @Override
    public void exitParameters(Python3Parser.ParametersContext ctx) {
        this.content = this.content.concat(") {");
        makeIndication();
    }

    @Override
    public void enterTypedargslist(Python3Parser.TypedargslistContext ctx) {
//        try {
//            this.content = this.content.concat(ctx.getText());
//        } catch (NullPointerException ex) {
//            System.out.println("Argument list is empty, skipped");
//        }
    }

    @Override
    public void exitTypedargslist(Python3Parser.TypedargslistContext ctx) {
        this.content = this.content.substring(0, this.content.length() - 1);
    }

    @Override
    public void enterTfpdef(Python3Parser.TfpdefContext ctx) {
        if (Mapper.isSpecial(ctx.getText())) {
            //this.content = this.content.concat(Mapper.getSpecial(ctx.getText()));
        } else {
            this.content = this.content.concat("Object " + ctx.getText());
        }
    }

    @Override
    public void exitTfpdef(Python3Parser.TfpdefContext ctx) {
        this.content = this.content.concat(Dictionary.COMMA);
    }

    @Override
    public void enterVarargslist(Python3Parser.VarargslistContext ctx) {

    }

    @Override
    public void exitVarargslist(Python3Parser.VarargslistContext ctx) {

    }

    @Override
    public void enterVfpdef(Python3Parser.VfpdefContext ctx) {

    }

    @Override
    public void exitVfpdef(Python3Parser.VfpdefContext ctx) {

    }

    @Override
    public void enterStmt(Python3Parser.StmtContext ctx) {

    }

    @Override
    public void exitStmt(Python3Parser.StmtContext ctx) {
        makeIndication();
    }

    /**
     * Python ma wbudowaną funkcję wyświetlającą informacje zaczynające się """ info """
     * oraz funckcję, która te informacje wywietla.
     * @param ctx
     */
    @Override
    public void enterSimple_stmt(Python3Parser.Simple_stmtContext ctx) {
        if (ctx.getChild(0).getText().matches("(^\"{3}.*)|(^\'{3}.*)")) {
            this.pythonInfo = ctx.getChild(0).getText();
            makeIndication();
            this.content = this.content.concat("/** CLASS_INFO = " + this.pythonInfo + " */");
        }
    }

    @Override
    public void exitSimple_stmt(Python3Parser.Simple_stmtContext ctx) {

    }

    @Override
    public void enterSmall_stmt(Python3Parser.Small_stmtContext ctx) {

    }

    @Override
    public void exitSmall_stmt(Python3Parser.Small_stmtContext ctx) {

    }

    @Override
    public void enterExpr_stmt(Python3Parser.Expr_stmtContext ctx) {
        if (ctx.getChildCount() > 1) {
            if (!this.variables.contains(ctx.getChild(0).getText())) {
                if (
                        (StringUtils.isNumeric(ctx.getChild(2).getText()))
                        || (hasExtendedStatement(ctx.getChild(2), Python3Parser.Arith_exprContext.class))
                ) {
                    this.content = this.content.concat("double" + Dictionary.SPACE);
                } else if (
                        ctx.getChild(2).getText().startsWith(Dictionary.QUOTA)
                                || ctx.getChild(2).getText().startsWith(Dictionary.APO)
                ) {
                    this.content = this.content.concat("String" + Dictionary.SPACE);
                }

                this.variables.add(ctx.getChild(0).getText());
            }

            this.content = this.content.concat(
                    ctx.getChild(0).getText()
                            + Dictionary.SPACE
                            + ctx.getChild(1).getText()
                            + Dictionary.SPACE
            );
        }
    }

    @Override
    public void exitExpr_stmt(Python3Parser.Expr_stmtContext ctx) {
        this.content = this.content.concat(Dictionary.SEMICOLON);
    }

    @Override
    public void enterTestlist_star_expr(Python3Parser.Testlist_star_exprContext ctx) {
       ParseTree leaf = ctx.getChild(0);
       boolean isFirst = ctx.getParent().getChild(0) == ctx;

       if (!isComplexStatement(leaf) && !isFirst) {
           this.content = this.content.concat(ctx.getText());
       }

    }

    @Override
    public void exitTestlist_star_expr(Python3Parser.Testlist_star_exprContext ctx) {
        //System.out.println(ctx.getText());
    }

    @Override
    public void enterAugassign(Python3Parser.AugassignContext ctx) {

    }

    @Override
    public void exitAugassign(Python3Parser.AugassignContext ctx) {

    }


    @Override
    public void enterFlow_stmt(Python3Parser.Flow_stmtContext ctx) {

    }

    @Override
    public void exitFlow_stmt(Python3Parser.Flow_stmtContext ctx) {

    }

    @Override
    public void enterBreak_stmt(Python3Parser.Break_stmtContext ctx) {
        this.content = this.content.concat("break" + Dictionary.SEMICOLON);
    }

    @Override
    public void exitBreak_stmt(Python3Parser.Break_stmtContext ctx) {

    }

    @Override
    public void enterContinue_stmt(Python3Parser.Continue_stmtContext ctx) {
        this.content = this.content.concat("continue" + Dictionary.SEMICOLON);
    }

    @Override
    public void exitContinue_stmt(Python3Parser.Continue_stmtContext ctx) {

    }

    @Override
    public void enterReturn_stmt(Python3Parser.Return_stmtContext ctx) {
        this.content = this.content.concat(Dictionary.RETURN);
    }

    @Override
    public void exitReturn_stmt(Python3Parser.Return_stmtContext ctx) {
        this.content = this.content.concat(Dictionary.SEMICOLON);
    }

    @Override
    public void enterRaise_stmt(Python3Parser.Raise_stmtContext ctx) {
        this.content = this.content.concat("throw ");
    }

    @Override
    public void exitRaise_stmt(Python3Parser.Raise_stmtContext ctx) {

    }

    @Override public void enterRaise_test(Python3Parser.Raise_testContext ctx) {
        if (hasExtendedStatement(ctx, Python3Parser.PowerContext.class )) {
            this.content = this.content.concat(getExtendedChild(ctx, Python3Parser.PowerContext.class ).getChild(0).getText());
        }
    }

    @Override public void exitRaise_test(Python3Parser.Raise_testContext ctx) {
        this.content = this.content.concat(Dictionary.SEMICOLON);

    }

    @Override
    public void enterImport_stmt(Python3Parser.Import_stmtContext ctx) {
        //this.content = this.content.concat();
        System.out.println("importstmt: " + ctx.getText());
    }

    @Override
    public void exitImport_stmt(Python3Parser.Import_stmtContext ctx) {

    }

    @Override
    public void enterImport_name(Python3Parser.Import_nameContext ctx) {
        this.content = "import "
                + ctx.getChild(1).getText()
                + Dictionary.SEMICOLON
                + Dictionary.NL
                + Dictionary.NL
                + this.content;
    }

    @Override
    public void exitImport_name(Python3Parser.Import_nameContext ctx) {

    }

    @Override
    public void enterImport_from(Python3Parser.Import_fromContext ctx) {
        String imp = "import " + ctx.getChild(1).getText() + Dictionary.SLASH;
        if (ctx.getChild(3).getChildCount() > 1) {
            for (int i = 0; i < ctx.getChild(3).getChildCount(); i++) {
                if (i % 2 == 0) {
                    this.content = imp
                                    + ctx.getChild(3).getChild(i).getText()
                                    + Dictionary.SEMICOLON
                                    + Dictionary.NL
                                    + this.content;
                }
            }
        }
    }

    @Override
    public void exitImport_from(Python3Parser.Import_fromContext ctx) {

    }

    @Override
    public void enterImport_as_name(Python3Parser.Import_as_nameContext ctx) {
        System.out.println("import as name: " + ctx.getText());
    }

    @Override
    public void exitImport_as_name(Python3Parser.Import_as_nameContext ctx) {

    }

    @Override
    public void enterDotted_as_name(Python3Parser.Dotted_as_nameContext ctx) {

    }

    @Override
    public void exitDotted_as_name(Python3Parser.Dotted_as_nameContext ctx) {

    }

    @Override
    public void enterImport_as_names(Python3Parser.Import_as_namesContext ctx) {

    }

    @Override
    public void exitImport_as_names(Python3Parser.Import_as_namesContext ctx) {

    }

    @Override
    public void enterDotted_as_names(Python3Parser.Dotted_as_namesContext ctx) {

    }

    @Override
    public void exitDotted_as_names(Python3Parser.Dotted_as_namesContext ctx) {

    }

    @Override
    public void enterDotted_name(Python3Parser.Dotted_nameContext ctx) {

    }

    @Override
    public void exitDotted_name(Python3Parser.Dotted_nameContext ctx) {

    }

    @Override
    public void enterFinally_suite(Python3Parser.Finally_suiteContext ctx) {
        this.content = this.content.concat(Dictionary.SPACE + "finally" + Dictionary.SPACE + Dictionary.OPEN_BRACE);
        makeIndication();
    }

    @Override
    public void exitFinally_suite(Python3Parser.Finally_suiteContext ctx) {
        makeIndication();
        //this.content = this.content.concat(Dictionary.CLOSE_BRACE);
    }

    @Override
    public void enterIf_stmt(Python3Parser.If_stmtContext ctx) {
        this.content = this.content.concat("if ");
    }

    @Override
    public void exitIf_stmt(Python3Parser.If_stmtContext ctx) {

    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterIf_suite(Python3Parser.If_suiteContext ctx) {
        this.content = this.content.concat(Dictionary.OPEN_BRACE);
        makeIndication();
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitIf_suite(Python3Parser.If_suiteContext ctx) {
        makeIndication();
        this.content = this.content.concat(Dictionary.CLOSE_BRACE);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterElif_suite(Python3Parser.Elif_suiteContext ctx) {
        this.content = this.content.concat(Dictionary.OPEN_BRACE);
        makeIndication();
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitElif_suite(Python3Parser.Elif_suiteContext ctx) {
        this.content = this.content.concat(Dictionary.CLOSE_BRACE);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterElse_suite(Python3Parser.Else_suiteContext ctx) {
        this.content = this.content.concat(" else ");
        this.content = this.content.concat(Dictionary.OPEN_BRACE);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitElse_suite(Python3Parser.Else_suiteContext ctx) {
        this.content = this.content.concat(Dictionary.CLOSE_BRACE);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterElif_test(Python3Parser.Elif_testContext ctx) {
        this.content = this.content.concat(" else if ");
        this.content = this.content.concat(Dictionary.OPEN_BRACKET);
        this.content = this.content.concat(
                ctx.getText().replace("and", " && ").replace("or", " || ")
        );
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitElif_test(Python3Parser.Elif_testContext ctx) {
        this.content = this.content.concat(Dictionary.CLOSE_BRACKET);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterAnd_op(Python3Parser.And_opContext ctx) {
        this.content = this.content.concat(" && ");
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterOr_op(Python3Parser.Or_opContext ctx) {
        this.content = this.content.concat(" || ");
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterIf_test(Python3Parser.If_testContext ctx) {
        this.content = this.content.concat(Dictionary.OPEN_BRACKET);
        this.content = this.content.concat(
                ctx.getText().replace("and", " && ").replace("or", " || ")
        );
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitIf_test(Python3Parser.If_testContext ctx) {
        this.content = this.content.concat(Dictionary.CLOSE_BRACKET);
    }


    @Override
    public void enterWhile_stmt(Python3Parser.While_stmtContext ctx) {
        this.content = this.content.concat("while ");
    }

    @Override
    public void exitWhile_stmt(Python3Parser.While_stmtContext ctx) {

    }

    @Override public void enterWhile_test(Python3Parser.While_testContext ctx) {
        this.content = this.content.concat(Dictionary.OPEN_BRACKET);
        this.content = this.content.concat(ctx.getText());
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitWhile_test(Python3Parser.While_testContext ctx) {
        this.content = this.content.concat(Dictionary.CLOSE_BRACKET + Dictionary.SPACE + Dictionary.OPEN_BRACE);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void enterWhile_suite(Python3Parser.While_suiteContext ctx) { }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override public void exitWhile_suite(Python3Parser.While_suiteContext ctx) {
        this.content = this.content.concat(Dictionary.CLOSE_BRACE);
    }

    @Override
    public void enterFor_stmt(Python3Parser.For_stmtContext ctx) {
        this.content = this.content.concat("for " + Dictionary.OPEN_BRACKET);
    }

    @Override
    public void exitFor_stmt(Python3Parser.For_stmtContext ctx) {
        makeIndication();
        this.content = this.content.concat(Dictionary.CLOSE_BRACE);
    }

    @Override
    public void enterTry_stmt(Python3Parser.Try_stmtContext ctx) {
        this.content = this.content.concat("try " + Dictionary.OPEN_BRACE);
        this.depth++;
        makeIndication();
    }

    @Override
    public void exitTry_stmt(Python3Parser.Try_stmtContext ctx) {
        this.content = this.content.concat(Dictionary.CLOSE_BRACE);
        this.depth--;
        makeIndication();
    }

    @Override
    public void enterExcept_clause(Python3Parser.Except_clauseContext ctx) {
        this.content = this.content.concat(
                Dictionary.SPACE
                        + "catch "
                        + Dictionary.OPEN_BRACKET
                        + ctx.getChild(1).getText()
            );
        /*
            Jeżeli występuje as
         */
        if ((ctx.getChildCount() > 3) && (ctx.getChild(2).getText().equals("as"))) {
            this.content = this.content.concat(Dictionary.SPACE + ctx.getChild(3).getText().toLowerCase());
        } else {
            this.content = this.content.concat(Dictionary.SPACE + "ex");
        }
    }

    @Override
    public void exitExcept_clause(Python3Parser.Except_clauseContext ctx) {

    }

    @Override
    public void enterExcept_clause_suite(Python3Parser.Except_clause_suiteContext ctx) {
        this.content = this.content.concat(Dictionary.CLOSE_BRACKET + Dictionary.SPACE + Dictionary.OPEN_BRACE);
        makeIndication();
    }

    @Override
    public void exitExcept_clause_suite(Python3Parser.Except_clause_suiteContext ctx) {
        this.content = this.content.concat(Dictionary.CLOSE_BRACE + Dictionary.SPACE );
    }

    @Override
    public void exitTry_suite(Python3Parser.Try_suiteContext ctx) {
        makeIndication();
        this.content = this.content.concat(Dictionary.CLOSE_BRACE);
    }

    @Override
    public void enterSuite(Python3Parser.SuiteContext ctx) {
        this.depth++;

        makeIndication();
    }

    @Override
    public void exitSuite(Python3Parser.SuiteContext ctx) {
        this.depth--;
        makeIndication();
        //this.content = this.content.concat(Dictionary.NL);
    }

    @Override
    public void enterTest(Python3Parser.TestContext ctx) {

    }

    @Override
    public void exitTest(Python3Parser.TestContext ctx) {

    }

    @Override
    public void enterComparison(Python3Parser.ComparisonContext ctx) {
        if ((ctx.getChildCount() > 2) && !(ctx.getChild(1) instanceof Python3Parser.Comp_opContext )) {
            this.content = this.content.concat(ctx.getText());
        }
    }

    @Override
    public void exitComparison(Python3Parser.ComparisonContext ctx) {

    }

    @Override
    public void enterComp_op(Python3Parser.Comp_opContext ctx) {
//        this.content = this.content.concat(
//                Dictionary.SPACE
//                + Dictionary.COMPARE
//                + Dictionary.SPACE
//        );
    }

    @Override
    public void exitComp_op(Python3Parser.Comp_opContext ctx) {

    }

    @Override
    public void enterStar_expr(Python3Parser.Star_exprContext ctx) {

    }

    @Override
    public void exitStar_expr(Python3Parser.Star_exprContext ctx) {

    }

    @Override
    public void enterExpr(Python3Parser.ExprContext ctx) {

    }

    @Override
    public void exitExpr(Python3Parser.ExprContext ctx) {
    }

    @Override
    public void enterXor_expr(Python3Parser.Xor_exprContext ctx) {

    }

    @Override
    public void exitXor_expr(Python3Parser.Xor_exprContext ctx) {

    }

    @Override
    public void enterAnd_expr(Python3Parser.And_exprContext ctx) {

    }

    @Override
    public void exitAnd_expr(Python3Parser.And_exprContext ctx) {

    }

    @Override
    public void enterShift_expr(Python3Parser.Shift_exprContext ctx) {

    }

    @Override
    public void exitShift_expr(Python3Parser.Shift_exprContext ctx) {

    }

    @Override
    public void enterArith_expr(Python3Parser.Arith_exprContext ctx) {
        if (
                (hasParentType(ctx.getParent(), Python3Parser.Return_stmtContext.class)
                && (hasParentType(ctx.getParent(), Python3Parser.ArglistContext.class))
                ))
            return;

        if (ctx.getChildCount() > 1) {
            for (int i = 0; i < ctx.getChildCount(); i++) {
                if (Mapper.isSpecial(ctx.getChild(i).getText())) {
                    this.content = this.content.concat(Mapper.getSpecial(ctx.getChild(i).getText()) + Dictionary.SPACE);
                } else {
                    this.content = this.content.concat(ctx.getChild(i).getText() + Dictionary.SPACE);
                }
            }
        }
    }

    @Override
    public void exitArith_expr(Python3Parser.Arith_exprContext ctx) {

    }

    @Override
    public void enterTerm(Python3Parser.TermContext ctx) {

    }

    @Override
    public void exitTerm(Python3Parser.TermContext ctx) {

    }

    @Override
    public void enterFactor(Python3Parser.FactorContext ctx) {

    }

    @Override
    public void exitFactor(Python3Parser.FactorContext ctx) {

    }

    /**
     *  Gdy wiecej niz 1 dziecko - wywolanie funkcji
     *
     * @param ctx PowerContext
     */
    @Override
    public void enterPower(Python3Parser.PowerContext ctx) {
        if (ctx.getChildCount() > 1) {
            if (Mapper.getBuildIn(ctx.getChild(0).getText()) != null) {
                this.content = this.content.concat( Mapper.getBuildIn( ctx.getChild(0).getText() ) );
            } else {
//                this.content = this.content.concat(
//                        ctx.getChild(0).getText()
//                );
            }
        }
    }

    @Override
    public void exitPower(Python3Parser.PowerContext ctx) {

    }

    @Override
    public void enterAtom(Python3Parser.AtomContext ctx) {
        if (Mapper.isSpecial(ctx.getText())) {
//            this.content = this.content.concat(
//                    Mapper.getSpecial(ctx.getText())
//            );
        }

        // System.out.println("atom: " + ctx.getText());
    }

    @Override
    public void exitAtom(Python3Parser.AtomContext ctx) {

    }

    @Override
    public void enterTestlist_comp(Python3Parser.Testlist_compContext ctx) {
        if (ctx.getParent().getText().startsWith("[")) {
            this.content = processNotIterableFor(this.content, ctx);
        }
    }

    @Override
    public void exitTestlist_comp(Python3Parser.Testlist_compContext ctx) {
//        if (ctx.getChildCount() > 1) {
//            this.content = this.content.concat(Dictionary.CLOSE_SQ_BRACKET);
//        }
    }

    @Override
    public void enterTrailer(Python3Parser.TrailerContext ctx) {
       if ((ctx.getChildCount() > 1) && (ctx.getChild(1).getText().equals("__doc__"))) {

           this.content =
                   this.content.replace(
                           ctx.getParent(). getText(),
                           this.pythonInfo.replace("\"\"", "")
                   );
       }
    }

    @Override
    public void exitTrailer(Python3Parser.TrailerContext ctx) {
       // this.content = this.content.concat(Dictionary.CLOSE_BRACKET);
    }

    @Override
    public void enterSubscriptlist(Python3Parser.SubscriptlistContext ctx) {

    }

    @Override
    public void exitSubscriptlist(Python3Parser.SubscriptlistContext ctx) {

    }

    @Override
    public void enterSubscript(Python3Parser.SubscriptContext ctx) {

    }

    @Override
    public void exitSubscript(Python3Parser.SubscriptContext ctx) {

    }

    @Override
    public void enterSliceop(Python3Parser.SliceopContext ctx) {

    }

    @Override
    public void exitSliceop(Python3Parser.SliceopContext ctx) {

    }

    @Override
    public void enterExprlist(Python3Parser.ExprlistContext ctx) {
        ParseTree testlist = ctx.getParent().getChild(3);
        if ( testlist != null) {
            if (testlist.getText().startsWith(Dictionary.APO) || testlist.getText().startsWith(Dictionary.QUOTA)) {
                this.content = this.content.concat("char " + ctx.getText());
            } else if (Character.isDigit(testlist.getText().charAt(1))) {
                this.content = this.content.concat("int " + ctx.getText());
            } else {
                this.content = this.content.concat("Object " + ctx.getText());
            }
        } else {
            this.content = this.content.concat("Object " + ctx.getText());
        }
    }

    @Override
    public void exitExprlist(Python3Parser.ExprlistContext ctx) {

    }

    @Override
    public void enterTestlist(Python3Parser.TestlistContext ctx) {
        if (ctx.getChildCount() > 1) {

        } else if (ctx.getParent() instanceof Python3Parser.Return_stmtContext) {
            if (!isComplexStatement(ctx.getParent().getChild(1))) {
                this.content = this.content.concat(ctx.getText());
            }
        } else {
            String text = Mapper.isSpecial(ctx.getText()) ? "" : ctx.getText();
            this.content = this.content.concat(
                    Dictionary.SPACE
                    + Dictionary.COLON
                    + Dictionary.SPACE
                    + text
            );
        }
    }

    @Override
    public void exitTestlist(Python3Parser.TestlistContext ctx) {
        if (ctx.getParent() instanceof Python3Parser.Return_stmtContext) {

        } else if (!this.content.endsWith("{")) {
            this.content = this.content.concat(
                    Dictionary.CLOSE_BRACKET
                    + Dictionary.SPACE
                    + Dictionary.OPEN_BRACE
            );
        }
    }

    @Override
    public void enterClassdef(Python3Parser.ClassdefContext ctx) {
        this.content = this.content.concat(Dictionary.CLASS_DEF);
        this.content = this.content.concat(ctx.getChild(1).getText());
        this.content = this.content.concat(Dictionary.SPACE);
        this.content = this.content.concat(Dictionary.OPEN_BRACE);
    }

    @Override
    public void exitClassdef(Python3Parser.ClassdefContext ctx) {
        this.content = this.content.concat(Dictionary.CLOSE_BRACE);
//        this.content = this.content.concat(Dictionary.NL);
        makeIndication();
    }

    /**
     * print i System.out.println za bardzo sie roznia od siebie, trzeba dopasowac jak ponizej, konkatenacja
     * stringów ze znakiem +
     *
     * @param ctx ArglistContext
     */
    @Override
    public void enterArglist(Python3Parser.ArglistContext ctx) {
        if (!hasExtendedStatement(ctx, Python3Parser.Arith_exprContext.class)) {
            this.content = this.content.concat(Dictionary.OPEN_BRACKET);
        }


        String prefix = ctx.getParent().getParent().getChild(0).getText();
        // print()
        switch (prefix) {
            case "print":
                for (int i = 0; i < ctx.getChildCount(); i++) {
                    if (ctx.getChild(i).getText().equals(Dictionary.COMMA)) {
                        this.content = this.content.concat(" + ");
                    } else {
                        this.content = this.content.concat(ctx.getChild(i).getText());
                    }
                }
                break;
            case "len":
                this.content = this.content.replace(
                        "size",
                        ctx.getChild(0).getText()
                                + Dictionary.DOT + "size"
                );
                break;
            case "abs":
                this.content = this.content.replace(
                        "abs",
                        "Math.abs"
                );
                break;
            case "range":
//                this.content = this.content.replace(
//                        "Range.between(",
//                        "Range.between(0, "
//                );
//                break;
            default:
               this.content = this.content.concat(ctx.getText());
        }

    }

    @Override
    public void exitArglist(Python3Parser.ArglistContext ctx) {
        if (!hasExtendedStatement(ctx, Python3Parser.Arith_exprContext.class)) {
            this.content = this.content.concat(Dictionary.CLOSE_BRACKET);
        }
    }

    @Override
    public void enterArgument(Python3Parser.ArgumentContext ctx) {
//        if (!Mapper.isSpecial(ctx.getText())) {
//            this.content = this.content.concat(ctx.getText());
//        }
    }

    @Override
    public void exitArgument(Python3Parser.ArgumentContext ctx) {

    }

    @Override
    public void enterComp_iter(Python3Parser.Comp_iterContext ctx) {

    }

    @Override
    public void exitComp_iter(Python3Parser.Comp_iterContext ctx) {

    }

    @Override
    public void enterComp_for(Python3Parser.Comp_forContext ctx) {

    }

    @Override
    public void exitComp_for(Python3Parser.Comp_forContext ctx) {

    }

    @Override
    public void enterComp_if(Python3Parser.Comp_ifContext ctx) {

    }

    @Override
    public void exitComp_if(Python3Parser.Comp_ifContext ctx) {

    }

    @Override
    public void enterStr(Python3Parser.StrContext ctx) {

    }

    @Override
    public void exitStr(Python3Parser.StrContext ctx) {
        if (hasComplexParentType(ctx, Python3Parser.For_stmtContext.class)) {
            if (!hasComplexParentType(ctx, Python3Parser.ArglistContext.class)
                    && !hasComplexParentType(ctx,Python3Parser.Testlist_compContext.class)
            ) {
                this.content = this.content.concat(".toCharArray()");
//                this.content =
//                        this.content.contains(".toCharArray().toCharArray()")
//                                ? this.content.replace(".toCharArray().toCharArray()", ".toCharArray()")
//                                : this.content;
            }


        }
    }

    /**
     * Function to make new lines and tabulation
     */
    private void makeIndication(){
        this.content = this.content.concat(Dictionary.NL);
        for (int i = 0; i < this.depth; i++) {
            this.content = this.content.concat(Dictionary.TAB);
        }
    }

    /**
     * Function to make new lines and tabulation
     */
    private String makeLocalIndication(){
        String indent = Dictionary.NL;
        for (int i = 0; i < this.depth; i++) {
            indent = indent.concat(Dictionary.TAB);
        }
        return indent;
    }

    /**
     *
     * Function to make new lines and tabulation
     * @param depth
     */
    private void makeIndication(int depth){
        this.content = this.content.concat(Dictionary.NL);
        for (int i = 0; i < depth; i++) {
            this.content = this.content.concat(Dictionary.TAB);
        }
    }

    private void openSourceClass() {
        this.content = this.content.concat(Dictionary.SOURCE_CLASS_INTRO);
        this.depth = this.depth + 2;
        makeIndication();
    }

    private void closeSourceClass() {
        makeIndication(--this.depth);
        this.content = this.content.concat(Dictionary.CLOSE_BRACE);
        makeIndication(--this.depth);
        this.content = this.content.concat(Dictionary.CLOSE_BRACE);
        makeIndication();
    }

    public String cutLastOccurence(String input, String needle, String replacement) {
        int pivot = input.lastIndexOf(needle);
        String firstPart = input.substring(0, pivot);
        String secondPart = input.substring(pivot)
                .replace(needle, replacement);

        return firstPart.concat(secondPart);
    }

    public String getLastPartFrom(String input, String needle) {
        int pivot = input.lastIndexOf(needle);

        return input.substring(pivot);
    }

    public String processNotIterableFor(String content, ParseTree node) {
        String iter = getComplexParent(node, Python3Parser.For_stmtContext.class).getChild(1).getText();

        if ((node.getText().startsWith("\"")) || (node.getText().startsWith("'"))) {
            String prefix = "String[] data = "
                    + node.getParent().getText()
                    .replace("[","{")
                    .replace("]","}")
                    + ";";
            String replacement = prefix + makeLocalIndication() + "for (String " + iter + " : data) {";
            return cutLastOccurence(
                    content,
                    getLastPartFrom(content, "for"),
                    replacement
            );
        } else if (Character.isDigit(node.getText().charAt(0))) {
            String prefix = "double[] data = "
                    + node.getParent().getText()
                    .replace("[","{")
                    .replace("]","}")
                    + ";\n";
            String replacement = prefix + makeLocalIndication() + "for (Double " + iter + " : data) {";
            return cutLastOccurence(
                    content,
                    getLastPartFrom(content, "for"),
                    replacement
            );
        } else {
            return content;
        }
    }
}
