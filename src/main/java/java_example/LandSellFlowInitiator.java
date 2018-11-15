package java_example;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import org.jetbrains.annotations.Nullable;

import java.security.PublicKey;
import java.util.List;

// `InitiatingFlow` means that we can start the flow directly (instead of
// solely in response to another flow).
@InitiatingFlow
// `StartableByRPC` means that a node operator can start the flow via RPC.
@StartableByRPC
// Like all states, implements `FlowLogic`.
public class LandSellFlowInitiator extends FlowLogic<Void> {
    private final Party owner;
    private final Party buyer;
    private final String landId;
    private final String landLocation;
    private final int landArea;
    private final int price;

    public LandSellFlowInitiator(Party owner, Party buyer, String landId, String landLocation, int landArea, int price) {
        this.owner = owner;
        this.buyer = buyer;
        this.landId = landId;
        this.landLocation = landLocation;
        this.landArea = landArea;
        this.price = price;
    }

    private final ProgressTracker progressTracker = new ProgressTracker();

    @Nullable
    @Override
    public ProgressTracker getProgressTracker() { return progressTracker; }

    // Must be marked `@Suspendable` to allow the flow to be suspended
    // mid-execution.
    @Suspendable
    @Override
    // Overrides `call`, where we define the logic executed by the flow.
    public Void call() throws FlowException {
        if (!(getOurIdentity().equals(owner)))
            throw new IllegalArgumentException("This flow must be stated by the land owner");

        // We pick an arbitrary notary from the network map. In practice,
        // it is always preferable to explicitly specify the notary to use.
        Party notarty = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

        // We build a transaction using a `TransactionBuilder`.
        TransactionBuilder txBuilder = new TransactionBuilder();

        // After creating the `TransactionBuilder`, we must specify which
        // notary it will use.
        txBuilder.setNotary(notarty);

        // We add the new LandState to the transaction.
        // Note that we also specify which contract class to use for
        // verification.
        LandState ourOutputState = new LandState(owner, buyer, landId, landLocation, landArea, price);
        txBuilder.addOutputState(ourOutputState, LandContract.ID);

        // We add the InitiateSell command to the transaction.
        // Note that we also specific who is required to sign the transaction.
        LandContract.Cmds.InitiateSell commandData = new LandContract.Cmds.InitiateSell();
        List<PublicKey> requiredSigners = ImmutableList.of(owner.getOwningKey(), buyer.getOwningKey());
        txBuilder.addCommand(commandData, requiredSigners);

        // We check that the transaction builder we've created meets the
        // contracts of the input and output states.
        txBuilder.verify(getServiceHub());

        // We finalise the transaction builder by signing it,
        // converting it into a `SignedTransaction`.
        SignedTransaction partlySignedTx = getServiceHub().signInitialTransaction(txBuilder);

        // We use `CollectSignaturesFlow` to automatically gather a
        // signature from each counterparty. The counterparty will need to
        // call `SignTransactionFlow` to decided whether or not to sign.
        FlowSession ownerSession = initiateFlow(owner);
        SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(partlySignedTx, ImmutableSet.of(ownerSession)));

        // We use `FinalityFlow` to automatically notarise the transaction
        // and have it recorded by all the `participants` of all the
        // transaction's states.
        subFlow(new FinalityFlow(fullySignedTx));

        return null;
    }
}
