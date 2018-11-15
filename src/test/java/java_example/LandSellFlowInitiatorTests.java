package java_example;

import com.google.common.collect.ImmutableList;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.TransactionState;
import net.corda.core.transactions.SignedTransaction;
import net.corda.testing.node.MockNetwork;
import net.corda.testing.node.StartedMockNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class LandSellFlowInitiatorTests {
    private MockNetwork network;
    private StartedMockNode nodeA;
    private StartedMockNode nodeB;
    private String landId = "F123456";
    private String landLocation = "Frankfurt";

    @Before
    public void setup() {
        network = new MockNetwork(ImmutableList.of("java_example"));
        nodeA = network.createPartyNode(null);
        nodeB = network.createPartyNode(null);
        network.runNetwork();
    }

    @After
    public void tearDown() {
        network.stopNodes();
    }

    /**
    @Test
    public void transactionConstructedByFlowUsesTheCorrectNotary() throws Exception {
        LandSellFlowInitiator flow = new LandSellFlowInitiator(
                nodeB.getInfo().getLegalIdentities().get(0),
                nodeA.getInfo().getLegalIdentities().get(0),
                landId, landLocation, 1, 1);
        CordaFuture<SignedTransaction> future = nodeA.startFlow(flow);
        network.runNetwork();
        SignedTransaction signedTransaction = future.get();

        assertEquals(1, signedTransaction.getTx().getOutputStates().size());
        TransactionState output = signedTransaction.getTx().getOutputs().get(0);

        assertEquals(network.getNotaryNodes().get(0).getInfo().getLegalIdentities().get(0), output.getNotary());
    }

    */
}