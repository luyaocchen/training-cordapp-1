package java_training;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.FinalityFlow;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import org.jetbrains.annotations.Nullable;

/* Our flow, automating the process of updating the ledger.
 * See src/main/java_examples/ for an example. */
public class TokenIssueFlow extends FlowLogic<SignedTransaction> {
    private final Party owner;
    private final int amount;

    public TokenIssueFlow(Party owner, int amount) {
        this.owner = owner;
        this.amount = amount;
    }

    private final ProgressTracker progressTracker = new ProgressTracker();

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {
        // We choose our transaction's notary (the notary prevents double-spends).
        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
        TransactionBuilder transactionBuilder = new TransactionBuilder(notary);
        transactionBuilder.addOutputState(new TokenState(getOurIdentity(), owner, amount), TokenContract.TOKENSTATE_ID);
        transactionBuilder.addCommand(new TokenContract.Commands.Issue(), getOurIdentity().getOwningKey());
        // We build our transaction.
        transactionBuilder.verify(getServiceHub());

        // We check our transaction is valid based on its contracts.

        SignedTransaction partlySignedTx = getServiceHub().signInitialTransaction(transactionBuilder);

        // We get the transaction notarised and recorded automatically by the platform.
        return subFlow(new FinalityFlow(partlySignedTx));
    }
}
