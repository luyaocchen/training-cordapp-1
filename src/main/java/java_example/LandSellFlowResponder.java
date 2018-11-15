package java_example;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.*;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.utilities.ProgressTracker;
import org.jetbrains.annotations.NotNull;

// `InitiatedBy` means that we will start this flow in response to a
// message from `LandIssueFlowInitiator`.
@InitiatedBy(LandSellFlowInitiator.class)
public class LandSellFlowResponder extends FlowLogic<Void> {
    private final FlowSession couterpartySession;

    // Responder flows always have a single constructor argument - a
    // `FlowSession` with the counterparty who initiated the flow.
    public LandSellFlowResponder(FlowSession couterpartySession) { this.couterpartySession = couterpartySession; }

    private final ProgressTracker progressTracker = new ProgressTracker();

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    @Suspendable
    @Override
    public Void call() throws FlowException {
        // If the counterparty requests our signature on a transaction using
        // `CollectSignaturesFlow`, we need to respond by invoking our own
        // `SignTransactionFlow` subclass.
        class SignTxFlow extends SignTransactionFlow {
            private SignTxFlow(@NotNull FlowSession otherSideSession, @NotNull ProgressTracker progressTracker) {
                super(otherSideSession, progressTracker);
            }

            // As part of `SignTransactionFlow`, the contracts of the
            // transaction's input and output states are run automatically.
            // Inside `checkTransaction`, we define our own additional logic
            // for checking the received transaction. If `checkTransaction`
            // throws an exception, we'll refuse to sign.

            @Override
            protected void checkTransaction(@NotNull SignedTransaction stx) throws FlowException {
                // Whatever checking you want to do...
            }
        }

        subFlow(new SignTxFlow(couterpartySession, SignTransactionFlow.tracker()));

        // Once the counterparty calls `FinalityFlow`, we will
        // automatically record the transaction if we are one of the
        // `participants` on one or more of the transaction's states.
        return null;
    }
}
