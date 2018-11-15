package java_example;

import net.corda.core.contracts.Contract;
import net.corda.core.identity.CordaX500Name;
import net.corda.testing.contracts.DummyState;
import net.corda.testing.core.DummyCommandData;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.node.MockServices;
import org.junit.Test;

import static net.corda.testing.node.NodeTestUtils.transaction;

public class LandContractTests {
    private final TestIdentity alice = new TestIdentity(new CordaX500Name("Alice", "", "DE"));
    private final TestIdentity bob = new TestIdentity(new CordaX500Name("Bob", "", "DE"));
    private final String landId = "F123456";
    private final String landLocation = "Frankfurt";
    private MockServices ledgerServices = new MockServices(new TestIdentity(new CordaX500Name("TestId", "", "GB")));
    private LandState landState = new LandState(alice.getParty(), bob.getParty(), landId, landLocation, 1, 1);

    @Test
    public void landContractImplementsContract() {
        assert (new LandContract() instanceof Contract);
    }

    @Test
    public void landContractRequiresZeroInputsInTheTransaction() {
        transaction(ledgerServices, tx -> {
            // Has an input, will fail.
            tx.input(LandContract.ID, landState);
            tx.output(LandContract.ID, landState);
            tx.command(alice.getPublicKey(), new LandContract.Cmds.InitiateSell());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has no input, will verify.
            tx.output(LandContract.ID, landState);
            tx.command(alice.getPublicKey(), new LandContract.Cmds.InitiateSell());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void landContractRequiresOneOutputInTheTransaction(){
        transaction(ledgerServices, tx -> {
            // Has two outputs, will fail.
            tx.output(LandContract.ID, landState);
            tx.output(LandContract.ID, landState);
            tx.command(alice.getPublicKey(), new LandContract.Cmds.InitiateSell());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has one output, will verify.
            tx.output(LandContract.ID, landState);
            tx.command(alice.getPublicKey(), new LandContract.Cmds.InitiateSell());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void landContractRequiresOneCommandInTheTransaction() {
        transaction(ledgerServices, tx -> {
            tx.output(LandContract.ID, landState);
            // Has two commands, will fail.
            tx.command(alice.getPublicKey(), new LandContract.Cmds.InitiateSell());
            tx.command(alice.getPublicKey(), new LandContract.Cmds.InitiateSell());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.output(LandContract.ID, landState);
            // Has one command, will verify.
            tx.command(alice.getPublicKey(), new LandContract.Cmds.InitiateSell());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void landContractRequiresTheTransactionsOutputToBeALandState() {
        transaction(ledgerServices, tx -> {
            // Has wrong output type, will fail.
            tx.output(LandContract.ID, new DummyState());
            tx.command(alice.getPublicKey(), new LandContract.Cmds.InitiateSell());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has correct output type, will verify.
            tx.output(LandContract.ID, landState);
            tx.command(alice.getPublicKey(), new LandContract.Cmds.InitiateSell());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void landContractRequiresTheTransactionsOutputToHaveAPositiveLandAreaPrice() {
        LandState zeroLandState = new LandState(alice.getParty(), bob.getParty(), landId, landLocation, -1, -1);
        LandState negativeLandState = new LandState(alice.getParty(), bob.getParty(), landId, landLocation, -1, -1);
        LandState positiveLandState = new LandState(alice.getParty(), bob.getParty(), landId, landLocation, 2, 2);

        transaction(ledgerServices, tx -> {
            // Has zero-amount LandState, will fail.
            tx.output(LandContract.ID, zeroLandState);
            tx.command(alice.getPublicKey(), new LandContract.Cmds.InitiateSell());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has negative-amount LandState, will fail.
            tx.output(LandContract.ID, negativeLandState);
            tx.command(alice.getPublicKey(), new LandContract.Cmds.InitiateSell());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has positive-amount LandState, will verify.
            tx.output(LandContract.ID, landState);
            tx.command(alice.getPublicKey(), new LandContract.Cmds.InitiateSell());
            tx.verifies();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Also has positive-amount LandState, will verify.
            tx.output(LandContract.ID, positiveLandState);
            tx.command(alice.getPublicKey(), new LandContract.Cmds.InitiateSell());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void landContractRequiresTheTransactionsCommandToBeAnInitiateSellCommand() {
        transaction(ledgerServices, tx -> {
            // Has wrong command type, will fail.
            tx.output(LandContract.ID, landState);
            tx.command(alice.getPublicKey(), DummyCommandData.INSTANCE);
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has correct command type, will verify.
            tx.output(LandContract.ID, landState);
            tx.command(alice.getPublicKey(), new LandContract.Cmds.InitiateSell());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void landContractRequiresTheIssuerToBeARequiredSignerInTheTransaction() {
        LandState landStateWhereBobIsIssuer = new LandState(bob.getParty(), alice.getParty(), landId, landLocation, 1, 1);

        transaction(ledgerServices, tx -> {
            // Issuer is not a required signer, will fail.
            tx.output(LandContract.ID, landState);
            tx.command(bob.getPublicKey(), new LandContract.Cmds.InitiateSell());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Issuer is also not a required signer, will fail.
            tx.output(LandContract.ID, landStateWhereBobIsIssuer);
            tx.command(alice.getPublicKey(), new LandContract.Cmds.InitiateSell());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Issuer is a required signer, will verify.
            tx.output(LandContract.ID, landState);
            tx.command(alice.getPublicKey(), new LandContract.Cmds.InitiateSell());
            tx.verifies();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Issuer is also a required signer, will verify.
            tx.output(LandContract.ID, landStateWhereBobIsIssuer);
            tx.command(bob.getPublicKey(), new LandContract.Cmds.InitiateSell());
            tx.verifies();
            return null;
        });
    }
 }