package java_example;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.util.List;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;

// Like all contracts, implements `Contract`
public class LandContract implements Contract {
    // Used tpo reference the contact in the transactions.
    public static final String ID = "java_example.LandContract";

    // Set of commands defined to execute our contract
    // Or the set of abstract methods, accessed through interface
    public interface Cmds extends CommandData {
        class InitiateSell implements Cmds {}
        class Sell implements Cmds {}
        class NoSell implements Cmds {}
    }

    @Override
    public void verify(@NotNull LedgerTransaction tx) throws IllegalArgumentException {
        CommandWithParties<Cmds> cmd = requireSingleCommand(tx.getCommands(), Cmds.class);

        if (cmd.getValue() instanceof Cmds.InitiateSell) {
            // Checking the shapes of the transaction.
            if (tx.getInputStates().size() != 0) throw new IllegalArgumentException("Land sell initiator should have no inputs.");
            if (tx.getOutputStates().size() != 1) throw new IllegalArgumentException("Land sell initiator should have one output.");
            if (tx.getCommands().size() != 1) throw new IllegalArgumentException("Land sell initiator should have one command");
            if (tx.outputsOfType(LandState.class).size() != 1) throw new IllegalArgumentException("Land sell initiator output have an LandState");

            // Grabbing the transaction's contents.
            final LandState landStateOutput = tx.outputsOfType(LandState.class).get(0);

            // Checking the transaction's contents.
            if (landStateOutput.getLandId().length() == 0)
                throw new IllegalArgumentException("Land sell initiator output should not have a zero-length land id.");
            if (landStateOutput.getLandArea() <= 0)
                throw new IllegalArgumentException("Land sell initiator output should not have a negative land area.");
            if (landStateOutput.getLandLocation().length() == 0)
                throw new IllegalArgumentException("Land sell initiator output should not have a zero-length land location.");
            if (landStateOutput.getPrice() <= 0)
                throw new IllegalArgumentException("Land sell initiator should not be negative.");

            // Checking the transaction's required signers.
            final List<PublicKey> requiredSigners = cmd.getSigners();
            if (!(requiredSigners.contains(landStateOutput.getOwner().getOwningKey())))
                throw new IllegalArgumentException("Land sell initiator output should have a owner as required signer.");
            if (!(requiredSigners.contains(landStateOutput.getBuyer().getOwningKey())))
                throw new IllegalArgumentException("Land sell initiator output should have a buyer as required signer.");

        }  else if (cmd.getValue() instanceof Cmds.Sell) {
            // Checking the shape of the transaction.
            if (tx.getInputStates().size() != 1) throw new IllegalArgumentException("Land selling should have one inputs.");
            if (tx.getOutputStates().size() != 1) throw new IllegalArgumentException("Land selling should have one output.");
            if (tx.getCommands().size() != 1) throw new IllegalArgumentException("land selling should have one command");
            if (tx.inputsOfType(LandState.class).size() != 1) throw new IllegalArgumentException("Land selling input should be an LandState.");
            if (tx.outputsOfType(LandState.class).size() != 1) throw new IllegalArgumentException("Land selling output should be an LandState.");

            // Grabbing the transaction's contents.
            final LandState landStateInput = tx.inputsOfType(LandState.class).get(0);
            final LandState landStateOutput = tx.outputsOfType(LandState.class).get(0);

            // Checking the transaction's contents.
            if (landStateInput.getOwner().equals(landStateOutput.getOwner()))
                throw new IllegalArgumentException("Land selling input and output should not have different owners.");
            if (!(landStateInput.getBuyer().equals(landStateOutput.getBuyer())))
                throw new IllegalArgumentException("Land selling input and output should have the same buyer.");
            if (landStateOutput.getLandId().length() == 0)
                throw new IllegalArgumentException("Land selling output should not have a zero-length land id.");
            if (landStateOutput.getLandArea() <= 0)
                throw new IllegalArgumentException("Land selling output should not have a negative land area.");
            if (landStateOutput.getLandLocation().length() == 0)
                throw new IllegalArgumentException("Land selling output should not have a zero-length land location.");
            if (landStateOutput.getPrice() <= 0)
                throw new IllegalArgumentException("Land selling should not be negative.");

            // Checking the transaction's required signers.
            final List<PublicKey> requiredSigners = cmd.getSigners();
            if (!(requiredSigners.contains(landStateOutput.getOwner().getOwningKey())))
                throw new IllegalArgumentException("Land selling output should have a owner as required signer.");
            if (!(requiredSigners.contains(landStateOutput.getBuyer().getOwningKey())))
                throw new IllegalArgumentException("Land selling output should have a buyer as required signer.");

        } else if (cmd.getValue() instanceof Cmds.NoSell) {
            // Checking the shape of the transaction.
            if (tx.getInputStates().size() != 1) throw new IllegalArgumentException("Land nosell should have one input.");
            if (tx.getOutputStates().size() != 0) throw new IllegalArgumentException("Land nosell should have no outputs.");
            if (tx.getCommands().size() != 1) throw new IllegalArgumentException("Land nosell should have one command");
            if (tx.inputsOfType(LandState.class).size() != 1) throw new IllegalArgumentException("Land nosell input should be an LandState.");

            // Grabbing the transaction's contents.
            final LandState landStateInput = tx.inputsOfType(LandState.class).get(0);

            // No checking of the transaction's contents required.

            // Checking the transaction's required signers.
            final List<PublicKey> requiredSigners = cmd.getSigners();
            if (!(requiredSigners.contains(landStateInput.getBuyer().getOwningKey())))
                throw new IllegalArgumentException("Land nosell should have input's buyer as a required signer.");
            if (!(requiredSigners.contains(landStateInput.getOwner().getOwningKey())))
                throw new IllegalArgumentException("Land nosell should have input's owner as a required signer.");

        } else throw new IllegalArgumentException("Unrecognised command.");
    }
}
