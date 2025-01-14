package net.sourceforge.htmlunit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import net.sourceforge.htmlunit.corejs.javascript.Context;
import net.sourceforge.htmlunit.corejs.javascript.ContextAction;
import net.sourceforge.htmlunit.corejs.javascript.ContextFactory;
import net.sourceforge.htmlunit.corejs.javascript.Scriptable;

/**
 * Unit tests for {@link org.mozilla.javascript.ScriptRuntime}
 *
 * @author Ahmed Ashour
 */
public class ScriptRuntimeTest {

    @Test
    public void functionDeclaredForwardInBlock() {
        final String script =
                  "function test () {\n"
                + "  if (true) {\n"
                + "    try {\n"
                + "      output += '' + foo;\n"
                + "    } catch (e) {\n"
                + "      output += 'exception';\n"
                + "    }\n"
                + "    function foo() {}\n"
                + "  }\n"
                + "};\n"
                + "var output = '';\n"
                + "test();\n"
                + "output";
        final int feature = Context.FEATURE_HTMLUNIT_FUNCTION_DECLARED_FORWARD_IN_BLOCK;
        test(script, "exception", feature, false);
        test(script, "function foo() {\n}", feature, true);
    }

    @Test
    public void functionDeclaredForwardInBlockAsVar() {
        final String script =
                  "function test () {\n"
                + "  if (true) {\n"
                + "    try {\n"
                + "      output += '' + foo;\n"
                + "    } catch (e) {\n"
                + "      output += 'exception';\n"
                + "    }\n"
                + "    var foo = function() {}\n"
                + "  }\n"
                + "};\n"
                + "var output = '';\n"
                + "test();\n"
                + "output";
        final int feature = Context.FEATURE_HTMLUNIT_FUNCTION_DECLARED_FORWARD_IN_BLOCK;
        test(script, "undefined", feature, false);
        test(script, "undefined", feature, true);
    }

    @Test
    public void functionDeclaredForwardInBlock2() {
        final String script =
                  "var output = '';\n"
                + "if (true) {\n"
                + "  try {\n"
                + "    output += '' + foo;\n"
                + "  } catch (e) {\n"
                + "    output += 'exception';\n"
                + "  }\n"
                + "  function foo() {}\n"
                + "}\n"
                + "output";
        final int feature = Context.FEATURE_HTMLUNIT_FUNCTION_DECLARED_FORWARD_IN_BLOCK;
        test(script, "exception", feature, false);
        test(script, "function foo() {\n}", feature, true);
    }

    private static void test(final String script, final Object expected,
            final int feature, final boolean featureValue) {
        final ContextFactory cf = new ContextFactory() {
            @Override
            protected boolean hasFeature(Context cx, int featureIndex) {
                if (featureIndex == feature) {
                    return featureValue;
                }
                return super.hasFeature(cx, featureIndex);
            }
        };

        final ContextAction<Object> action = new ContextAction<Object>() {
            @Override
            public Object run(final Context cx) {
                try {
                    Scriptable scope = cx.initSafeStandardObjects();
                    final Object o = cx.evaluateString(scope, script, "test_script", 1, null);
                    assertEquals(expected, o);
                    return o;
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };

        Utils.runWithOptimizationLevel(cf, action, -1);
    }

    @Test
    public void enumChangeObject() {
        final String script =
                "var value = {\n"
                + "'xxx': 'testxxx',\n"
                + "'50': 'test50',\n"
                + "'zzz': 'testzzz',\n"
                + "'100': 'test100',\n"
                + "'0': 'test0',\n"
                + "'yyy': 'testyyy'\n"
                + "};\n"
                + " var output = '';\n"
                + "for (var x in value) {\n"
                + "   output += x + ',';\n"
                + "};"
                + "output";
        final int feature = Context.FEATURE_HTMLUNIT_ENUM_NUMBERS_FIRST;
        test(script, "xxx,50,zzz,100,0,yyy,", feature, false);
        test(script, "0,50,100,xxx,zzz,yyy,", feature, true);
    }

    private static void test(final String script, final Object expected) {
        final ContextAction<Object> action = new ContextAction<Object>() {
            @Override
            public Object run(final Context cx) {
                try {
                    Scriptable scope = cx.initSafeStandardObjects();
                    final Object o = cx.evaluateString(scope, script, "test_script", 1, null);
                    assertEquals(expected, o);
                    return o;
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };

        Utils.runWithAllOptimizationLevels(action);
    }

    /**
     * @throws Exception if the test fails
     */
    @Test
    public void apply() throws Exception {
        final String script =
                  "  var myObject = {'length': 2, '0': 'eat', '1': 'bananas'};\n"
                + "  function test() {\n"
                + "    test2.apply(null, myObject);\n"
                + "  }\n"
                + "\n"
                + "  function test2() {\n"
                + "    output += arguments.length;\n"
                + "    for (var i in arguments) {\n"
                + "      output += ', ' + arguments[i];\n"
                + "    }\n"
                + "  }\n"
                + "var output = '';\n"
                + "test();"
                + "output";

        final String expected = "2, eat, bananas";
        test(script, expected);
    }
}
