package converters;

import models.TuringMachine;
import models.UnrestrictedGrammar;
import utils.GrammarUtils;
import utils.TuringMachineUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static models.UnrestrictedGrammar.GrammarSymbol;
import static models.UnrestrictedGrammar.Production;
import static models.TuringMachine.State;
import static models.TuringMachine.TransitionContext;
import static models.TuringMachine.Transition;
import static models.TuringMachine.Transition.Direction;
import static models.UnrestrictedGrammar.Production.Type;

public class LbaToCSGrammar {
    private static final String L = "%";
    private static final String R = "$";
    //private static final Set<String> ALPHABET = Set.of("0", "1");
    private static final Set<String> ALPHABET = Set.of("1");

    public static UnrestrictedGrammar convert(TuringMachine turingMachine) {
        Set<Production> productions = new LinkedHashSet<>();
        Set<GrammarSymbol> alphabetSymbols = ALPHABET.stream()
                .map(s -> new GrammarSymbol(s, true))
                .collect(Collectors.toSet());

        GrammarSymbol a1 = new GrammarSymbol("A1", false);
        GrammarSymbol a2 = new GrammarSymbol("A2", false);

        State init = turingMachine.getInitialState();

        for (var aSym : alphabetSymbols) {
            String a = aSym.getValue();
            // 4.1
            productions.add(new Production(
                    List.of(a1), List.of(createSymbol(init.getValue(), L, a, a), a2),
                    Type.TAPE_GENERATING
            ));
            // 4.2
            productions.add(new Production(
                    List.of(a2), List.of(createSymbol(a, a), a2),
                    Type.TAPE_GENERATING
            ));
            // 4.3
            productions.add(new Production(
                    List.of(a2), List.of(createSymbol(a, a, R)),
                    Type.TAPE_GENERATING
            ));
        }

        Map<TransitionContext, Transition> func = turingMachine.getTransitionFunc();

        for (var state : turingMachine.getStates()) {
            if (turingMachine.getFinalStates().contains(state)) {
                continue;
            }

            for (Map.Entry<TransitionContext, Transition> entry : func.entrySet()) {
                TransitionContext ctx = entry.getKey();
                Transition trans = entry.getValue();

                State q = ctx.getState();
                State p = trans.getContextTo().getState();

                if (!q.equals(state)) {
                    continue;
                }

                for (var x : getPossTapeSymbols(turingMachine)) {
                    for (var z : getPossTapeSymbols(turingMachine)) {
                        for (var aSym : alphabetSymbols) {
                            for (var bSym : alphabetSymbols) {
                                String a = aSym.getValue();
                                String b = bSym.getValue();
                                String y = trans.getContextTo().getTapeSym();

                                if (
                                        ctx.getTapeSym().equals(L)
                                                && y.equals(L)
                                                && trans.getDirection().equals(Direction.RIGHT)
                                ) {
                                    // 5.1
                                    productions.add(new Production(
                                            List.of(createSymbol(q.getValue(), L, x, a)),
                                            List.of(createSymbol(L, p.getValue(), x, a)),
                                            Type.TM_EMULATING
                                    ));
                                } else if (
                                        ctx.getTapeSym().equals(x)
                                                && trans.getDirection().equals(Direction.LEFT)
                                ) {
                                    // 5.2
                                    productions.add(new Production(
                                            List.of(createSymbol(L, q.getValue(), x, a)),
                                            List.of(createSymbol(p.getValue(), L, y, a)),
                                            Type.TM_EMULATING
                                    ));
                                    // 6.2
                                    productions.add(new Production(
                                            List.of(createSymbol(z, b), createSymbol(q.getValue(), x, a)),
                                            List.of(createSymbol(p.getValue(), z, b), createSymbol(y, a)),
                                            Type.TM_EMULATING
                                    ));
                                    // 6.4
                                    productions.add(new Production(
                                            List.of(createSymbol(L, z, b), createSymbol(q.getValue(), x, a)),
                                            List.of(createSymbol(L, p.getValue(), z, b), createSymbol(y, a)),
                                            Type.TM_EMULATING
                                    ));
                                    // 7.3
                                    productions.add(new Production(
                                            List.of(createSymbol(z, b), createSymbol(q.getValue(), x, a, R)),
                                            List.of(createSymbol(p.getValue(), z, b), createSymbol(y, a, R)),
                                            Type.TM_EMULATING
                                    ));
                                    // this production should be in case |w|=2
                                    productions.add(new Production(
                                            List.of(createSymbol(L, z, b), createSymbol(q.getValue(), x, a, R)),
                                            List.of(createSymbol(L, p.getValue(), z, b), createSymbol(y, a, R)),
                                            Type.TM_EMULATING
                                    ));
                                } else if (
                                        ctx.getTapeSym().equals(x)
                                                && trans.getDirection().equals(Direction.RIGHT)
                                ) {
                                    // 5.3
                                    productions.add(new Production(
                                            List.of(createSymbol(L, q.getValue(), x, a), createSymbol(z, b)),
                                            List.of(createSymbol(L, y, a), createSymbol(p.getValue(), z, b)),
                                            Type.TM_EMULATING
                                    ));
                                    // this production should be in case |w|=2
                                    productions.add(new Production(
                                            List.of(createSymbol(L, q.getValue(), x, a), createSymbol(z, b, R)),
                                            List.of(createSymbol(L, y, a), createSymbol(p.getValue(), z, b, R)),
                                            Type.TM_EMULATING
                                    ));
                                    // 6.1
                                    productions.add(new Production(
                                            List.of(createSymbol(q.getValue(), x, a), createSymbol(z, b)),
                                            List.of(createSymbol(y, a), createSymbol(p.getValue(), z, b)),
                                            Type.TM_EMULATING
                                    ));
                                    // 6.3
                                    productions.add(new Production(
                                            List.of(createSymbol(q.getValue(), x, a), createSymbol(z, b, R)),
                                            List.of(createSymbol(y, a), createSymbol(p.getValue(), z, b, R)),
                                            Type.TM_EMULATING
                                    ));
                                    // 7.1
                                    productions.add(new Production(
                                            List.of(createSymbol(q.getValue(), x, a, R)),
                                            List.of(createSymbol(y, a, p.getValue(), R)),
                                            Type.TM_EMULATING
                                    ));
                                } else if (
                                        ctx.getTapeSym().equals(R)
                                                && y.equals(R)
                                                && trans.getDirection().equals(Direction.LEFT)
                                ) {
                                    // 7.2
                                    productions.add(new Production(
                                            List.of(createSymbol(x, a, q.getValue(), R)),
                                            List.of(createSymbol(p.getValue(), x, a, R)),
                                            Type.TM_EMULATING
                                    ));
                                } else if (
                                        ctx.getTapeSym().equals(L)
                                                && y.equals(L)
                                                && trans.getDirection().equals(Direction.STAY)
                                ) {
                                    // stay on left side
                                    productions.add(new Production(
                                            List.of(createSymbol(q.getValue(), L, x, a)),
                                            List.of(createSymbol(p.getValue(), L, x, a)),
                                            Type.TM_EMULATING
                                    ));
                                }
                                else if (
                                        ctx.getTapeSym().equals(R)
                                                && y.equals(R)
                                                && trans.getDirection().equals(Direction.STAY)
                                ) {
                                    // stay on right side
                                    productions.add(new Production(
                                            List.of(createSymbol(x, a, q.getValue(), R)),
                                            List.of(createSymbol(x, a, p.getValue(), R)),
                                            Type.TM_EMULATING
                                    ));
                                } else if (
                                        ctx.getTapeSym().equals(x)
                                                && trans.getDirection().equals(Direction.STAY)
                                ) {
                                    // stay in middle
                                    productions.add(new Production(
                                            List.of(createSymbol(q.getValue(), x, a)),
                                            List.of(createSymbol(p.getValue(), y, a)),
                                            Type.TM_EMULATING
                                    ));
                                }
                            }
                        }
                    }
                }
            }
        }

        for (var q : turingMachine.getFinalStates()) {
            for (var x : getPossTapeSymbols(turingMachine)) {
                for (var aSym : alphabetSymbols) {
                    String a = aSym.getValue();
                    // 8.1
                    productions.add(new Production(
                            List.of(createSymbol(q.getValue(), L, x, a)),
                            List.of(aSym),
                            Type.WORD_RESTORING
                    ));
                    // 8.2
                    productions.add(new Production(
                            List.of(createSymbol(L, q.getValue(), x, a)),
                            List.of(aSym),
                            Type.WORD_RESTORING
                    ));
                    // 8.3
                    productions.add(new Production(
                            List.of(createSymbol(q.getValue(), x, a)),
                            List.of(aSym),
                            Type.WORD_RESTORING
                    ));
                    // 8.4
                    productions.add(new Production(
                            List.of(createSymbol(q.getValue(), x, a, R)),
                            List.of(aSym),
                            Type.WORD_RESTORING
                    ));
                    // 8.5
                    productions.add(new Production(
                            List.of(createSymbol(x, a, q.getValue(), R)),
                            List.of(aSym),
                            Type.WORD_RESTORING
                    ));
                }
            }
        }

        for (var x : getPossTapeSymbols(turingMachine)) {
            for (var aSym : alphabetSymbols) {
                for (var bSym : alphabetSymbols) {
                    String a = aSym.getValue();
                    String b = bSym.getValue();

                    // 9.1
                    productions.add(new Production(
                            List.of(aSym, createSymbol(x, b)),
                            List.of(aSym, bSym),
                            Type.WORD_RESTORING
                    ));
                    // 9.2
                    productions.add(new Production(
                            List.of(aSym, createSymbol(x, b)),
                            List.of(aSym, bSym),
                            Type.WORD_RESTORING
                    ));
                    // 9.3
                    productions.add(new Production(
                            List.of(createSymbol(x, a), bSym),
                            List.of(aSym, bSym),
                            Type.WORD_RESTORING
                    ));
                    // 9.4
                    productions.add(new Production(
                            List.of(createSymbol(L, x, a), bSym),
                            List.of(aSym, bSym),
                            Type.WORD_RESTORING
                    ));
                }
            }
        }

        return new UnrestrictedGrammar(productions, a1);
    }

    private static Set<String> getPossTapeSymbols(TuringMachine turingMachine) {
        Set<String> possTapeSyms = new HashSet<>();

        for (Map.Entry<TransitionContext, Transition> entry : turingMachine.getTransitionFunc().entrySet()) {
            TransitionContext ctx = entry.getKey();
            Transition trans = entry.getValue();

            String ctxSym = ctx.getTapeSym();
            String transSym = trans.getContextTo().getTapeSym();

            if (!ctxSym.equals(L) && !ctxSym.equals(R)) {
                possTapeSyms.add(ctxSym);
            }
            if (!transSym.equals(L) && !transSym.equals(R)) {
                possTapeSyms.add(transSym);
            }
        }

        return possTapeSyms;
    }

    private static GrammarSymbol createSymbol(String a, String b, String c, String d) {
        return new GrammarSymbol("[" + a + "," + b + "," + c + "," + d + "]", false);
    }

    private static GrammarSymbol createSymbol(String a, String b, String c) {
        return new GrammarSymbol("[" + a + "," + b + "," + c + "]", false);
    }

    private static GrammarSymbol createSymbol(String a, String b) {
        return new GrammarSymbol("[" + a + "," + b + "]", false);
    }
}
