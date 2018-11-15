package java_training;

/* Our contract, governing how our state will evolve over time.
 * See src/main/java/java_example/LandContract.java for an example. */

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.util.List;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;

public class TokenContract implements Contract {
    public static String TOKENSTATE_ID = "java_training.TokenContract";

    @Override
    public void verify(@NotNull LedgerTransaction tx) throws IllegalArgumentException {
        CommandWithParties<Commands> command = requireSingleCommand(tx.getCommands(), Commands.class);
        if (command.getValue() instanceof Commands.Issue) {
            // Input checks - contract shape rules
            if (tx.getInputStates().size() != 0) throw new IllegalArgumentException("Token contract should have no input.");
            if (tx.getOutputStates().size() != 1) throw new IllegalArgumentException("Token contract should have one output.");
            if (tx.getCommands().size() != 1) throw new IllegalArgumentException("Token contract should have one command.");
            if (tx.outputsOfType(TokenState.class).size() != 1) throw new IllegalArgumentException("Token contract output should be TokenState");

            // Contract specific checks - our contract validation rules
            final TokenState tokenStateOutput = tx.outputsOfType(TokenState.class).get(0);

            if (tokenStateOutput.getAmount() <= 0) throw new IllegalArgumentException("Token amount should not be negative.");

            // Parties as signer checks
            final List<PublicKey>  requiredSigners = command.getSigners();
            if (!(requiredSigners.contains(tokenStateOutput.getIssuer().getOwningKey())))
                throw new IllegalArgumentException("Token issuer as signer is required");
        } else
            throw new IllegalArgumentException("Unrecognised command!");
    }

    public interface Commands extends CommandData {
        class Issue implements Commands {}
    }
}
