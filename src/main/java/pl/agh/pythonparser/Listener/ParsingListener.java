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
    ArrayList<String> variables = new ArrayList<>();
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
    public void enterSingle_input(Python3Parser.Single_inputContext ctx) {

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
        this.content = this.content.concat(Dictionary.MAIN_FUNCTION_INTRO);
        makeIndication(1);
        makeIndication(1);
        this.content = this.content.concat(Dictionary.CLOSE_BRACE);
        makeIndication(0);
        this.content = this.content.concat(Dictionary.CLOSE_BRACE);

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
    public void enterEval_input(Python3Parser.Eval_inputContext ctx) {

    }

    @Override
    public void exitEval_input(Python3Parser.Eval_inputContext ctx) {

    }

    @Override
    public void enterDecorator(Python3Parser.DecoratorContext ctx) {

    }

    @Override
    public void exitDecorator(Python3Parser.DecoratorContext ctx) {

    }

    @Override
    public void enterDecorators(Python3Parser.DecoratorsContext ctx) {
        System.out.println(ctx.getText());
    }

    @Override
    public void exitDecorators(Python3Parser.DecoratorsContext ctx) {

    }

    @Override
    public void enterDecorated(Python3Parser.DecoratedContext ctx) {

    }

    @Override
    public void exitDecorated(Python3Parser.DecoratedContext ctx) {

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
                if (StringUtils.isNumeric(ctx.getChild(2).getText())) {
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
    public void enterDel_stmt(Python3Parser.Del_stmtContext ctx) {

    }

    @Override
    public void exitDel_stmt(Python3Parser.Del_stmtContext ctx) {

    }

    @Override
    public void enterPass_stmt(Python3Parser.Pass_stmtContext ctx) {

    }

    @Override
    public void exitPass_stmt(Python3Parser.Pass_stmtContext ctx) {

    }

    @Override
    public void enterFlow_stmt(Python3Parser.Flow_stmtContext ctx) {

    }

    @Override
    public void exitFlow_stmt(Python3Parser.Flow_stmtContext ctx) {

    }

    @Override
    public void enterBreak_stmt(Python3Parser.Break_stmtContext ctx) {

    }

    @Override
    public void exitBreak_stmt(Python3Parser.Break_stmtContext ctx) {

    }

    @Override
    public void enterContinue_stmt(Python3Parser.Continue_stmtContext ctx) {

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
    public void enterYield_stmt(Python3Parser.Yield_stmtContext ctx) {

    }

    @Override
    public void exitYield_stmt(Python3Parser.Yield_stmtContext ctx) {

    }

    @Override
    public void enterRaise_stmt(Python3Parser.Raise_stmtContext ctx) {

    }

    @Override
    public void exitRaise_stmt(Python3Parser.Raise_stmtContext ctx) {

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
        System.out.println("import_name: " + ctx.getText());
    }

    @Override
    public void exitImport_name(Python3Parser.Import_nameContext ctx) {

    }

    @Override
    public void enterImport_from(Python3Parser.Import_fromContext ctx) {
        System.out.println("import from : " + ctx.getText());
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
    public void enterGlobal_stmt(Python3Parser.Global_stmtContext ctx) {

    }

    @Override
    public void exitGlobal_stmt(Python3Parser.Global_stmtContext ctx) {

    }

    @Override
    public void enterNonlocal_stmt(Python3Parser.Nonlocal_stmtContext ctx) {

    }

    @Override
    public void exitNonlocal_stmt(Python3Parser.Nonlocal_stmtContext ctx) {

    }

    @Override
    public void enterAssert_stmt(Python3Parser.Assert_stmtContext ctx) {
        System.out.println("ASSERTION");
    }

    @Override
    public void exitAssert_stmt(Python3Parser.Assert_stmtContext ctx) {

    }

    @Override
    public void enterCompound_stmt(Python3Parser.Compound_stmtContext ctx) {

    }

    @Override
    public void exitCompound_stmt(Python3Parser.Compound_stmtContext ctx) {

    }

    @Override
    public void enterIf_stmt(Python3Parser.If_stmtContext ctx) {

    }

    @Override
    public void exitIf_stmt(Python3Parser.If_stmtContext ctx) {

    }

    @Override
    public void enterWhile_stmt(Python3Parser.While_stmtContext ctx) {

    }

    @Override
    public void exitWhile_stmt(Python3Parser.While_stmtContext ctx) {

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

    }

    @Override
    public void exitTry_stmt(Python3Parser.Try_stmtContext ctx) {

    }

    @Override
    public void enterWith_stmt(Python3Parser.With_stmtContext ctx) {

    }

    @Override
    public void exitWith_stmt(Python3Parser.With_stmtContext ctx) {

    }

    @Override
    public void enterWith_item(Python3Parser.With_itemContext ctx) {

    }

    @Override
    public void exitWith_item(Python3Parser.With_itemContext ctx) {

    }

    @Override
    public void enterExcept_clause(Python3Parser.Except_clauseContext ctx) {

    }

    @Override
    public void exitExcept_clause(Python3Parser.Except_clauseContext ctx) {

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
    public void enterTest_nocond(Python3Parser.Test_nocondContext ctx) {

    }

    @Override
    public void exitTest_nocond(Python3Parser.Test_nocondContext ctx) {

    }

    @Override
    public void enterLambdef(Python3Parser.LambdefContext ctx) {

    }

    @Override
    public void exitLambdef(Python3Parser.LambdefContext ctx) {

    }

    @Override
    public void enterLambdef_nocond(Python3Parser.Lambdef_nocondContext ctx) {

    }

    @Override
    public void exitLambdef_nocond(Python3Parser.Lambdef_nocondContext ctx) {

    }

    @Override
    public void enterOr_test(Python3Parser.Or_testContext ctx) {

    }

    @Override
    public void exitOr_test(Python3Parser.Or_testContext ctx) {

    }

    @Override
    public void enterAnd_test(Python3Parser.And_testContext ctx) {

    }

    @Override
    public void exitAnd_test(Python3Parser.And_testContext ctx) {

    }

    @Override
    public void enterNot_test(Python3Parser.Not_testContext ctx) {

    }

    @Override
    public void exitNot_test(Python3Parser.Not_testContext ctx) {

    }

    @Override
    public void enterComparison(Python3Parser.ComparisonContext ctx) {
        if (ctx.getChildCount() > 1) {
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
        //System.out.println("INSIDE EXPR");

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
//            if (ctx.getChild(Python3Parser.Testlist_compContext.class, 0) != null) {
//                this.content = this.content.concat("comp " + ctx.getText());
//            } else {
//                this.content = this.content.concat(" in ");
//            }
        } else if (ctx.getParent() instanceof Python3Parser.Return_stmtContext) {
            if (!isComplexStatement(ctx.getParent().getChild(1))) {
                this.content = this.content.concat(ctx.getText());
            }
        } else {
            String text = Mapper.isSpecial(ctx.getText()) ? "" : ctx.getText();
            this.content = this.content.concat(
                    Dictionary.SPACE
                    + "in"
                    + Dictionary.SPACE
                    + text
            );
        }
    }

    @Override
    public void exitTestlist(Python3Parser.TestlistContext ctx) {
        if (ctx.getParent() instanceof Python3Parser.Return_stmtContext) {

        } else {
            this.content = this.content.concat(
                    Dictionary.CLOSE_BRACKET
                    + Dictionary.SPACE
                    + Dictionary.OPEN_BRACE
            );
        }
    }

    @Override
    public void enterDictorsetmaker(Python3Parser.DictorsetmakerContext ctx) {

    }

    @Override
    public void exitDictorsetmaker(Python3Parser.DictorsetmakerContext ctx) {

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
               // this.content = this.content.concat(ctx.getText());
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
    public void enterYield_expr(Python3Parser.Yield_exprContext ctx) {

    }

    @Override
    public void exitYield_expr(Python3Parser.Yield_exprContext ctx) {

    }

    @Override
    public void enterYield_arg(Python3Parser.Yield_argContext ctx) {

    }

    @Override
    public void exitYield_arg(Python3Parser.Yield_argContext ctx) {

    }

    @Override
    public void enterStr(Python3Parser.StrContext ctx) {
        //
    }

    @Override
    public void exitStr(Python3Parser.StrContext ctx) {

    }

    @Override
    public void enterNumber(Python3Parser.NumberContext ctx) {

    }

    @Override
    public void exitNumber(Python3Parser.NumberContext ctx) {

    }

    @Override
    public void enterInteger(Python3Parser.IntegerContext ctx) {

    }

    @Override
    public void exitInteger(Python3Parser.IntegerContext ctx) {

    }

    @Override
    public void visitTerminal(TerminalNode terminalNode) {

    }

    @Override
    public void visitErrorNode(ErrorNode errorNode) {

    }

    @Override
    public void enterEveryRule(ParserRuleContext parserRuleContext) {

    }

    @Override
    public void exitEveryRule(ParserRuleContext parserRuleContext) {

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
        this.depth++;
        makeIndication();
    }

    private void closeSourceClass() {
        makeIndication(--this.depth);
        this.content = this.content.concat(Dictionary.CLOSE_BRACE);
        makeIndication();
    }

    @Override
    public void enterElif_stmt(Python3Parser.Elif_stmtContext ctx){
        this.content = this.content.concat("else if" + Dictionary.SPACE);
    }

    @Override
    public void exitElif_stmt(Python3Parser.Elif_stmtContext ctx){

    }

    @Override
    public void enterElse_stmt(Python3Parser.Else_stmtContext ctx){
        this.content = this.content.concat(ctx.ELSE().getText());
    }

    @Override
    public void exitElse_stmt(Python3Parser.Else_stmtContext ctx){

    }

}
