package formallang;

import java.util.List;
import java.util.Set;

public class UnrestrictedGrammar {
    private final Set<GrammarSymbol> terminals;
    private final Set<GrammarSymbol> variables;
    private final Set<Production> productions;
    private final GrammarSymbol startSymbol;

    public UnrestrictedGrammar(
            Set<GrammarSymbol> terminals,
            Set<GrammarSymbol> variables,
            Set<Production> productions,
            GrammarSymbol startSymbol
    ) {
        this.terminals = terminals;
        this.variables = variables;
        this.productions = productions;
        this.startSymbol = startSymbol;
    }

    public Set<GrammarSymbol> getTerminals() {
        return terminals;
    }

    public Set<GrammarSymbol> getVariables() {
        return variables;
    }

    public Set<Production> getProductions() {
        return productions;
    }

    public GrammarSymbol getStartSymbol() {
        return startSymbol;
    }

    public static class Production {
        private final List<GrammarSymbol> head;
        private final List<GrammarSymbol> body;

        public Production(List<GrammarSymbol> head, List<GrammarSymbol> body) {
            this.head = head;
            this.body = body;
        }

        public List<GrammarSymbol> getHead() {
            return head;
        }

        public List<GrammarSymbol> getBody() {
            return body;
        }
    }

    public static class GrammarSymbol {
        private final String value;
        private final boolean isTerminal;

        public GrammarSymbol(String value, boolean isTerminal) {
            this.value = value;
            this.isTerminal = isTerminal;
        }

        public String getValue() {
            return value;
        }

        public boolean isTerminal() {
            return isTerminal;
        }
    }
}