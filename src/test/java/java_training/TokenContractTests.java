package java_training;

import net.corda.core.contracts.Contract;
import net.corda.core.identity.CordaX500Name;
import net.corda.testing.contracts.DummyState;
import net.corda.testing.core.DummyCommandData;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.node.MockServices;
import org.junit.Test;

import static net.corda.testing.node.NodeTestUtils.transaction;

public class TokenContractTests {
    private final TestIdentity alice = new TestIdentity(new CordaX500Name("Alice", "", "DE"));
    private final TestIdentity bob = new TestIdentity(new CordaX500Name("Bob", "", "DE"));
    private MockServices ledgerServices = new MockServices(new TestIdentity(new CordaX500Name("TestId", "", "DE")));
    private TokenState tokenState = new TokenState(alice.getParty(), bob.getParty(), 1);

    @Test
    public void tokenContractImplementsContract () {
        assert(new TokenContract() instanceof Contract);
    }

    @Test
    public void tokenContractRequiresZeroInputsInTheTransaction() {
        transaction(ledgerServices, tx -> {
            // Has an input, will fail.
            tx.input(TokenContract.TOKENSTATE_ID, tokenState);
            tx.output(TokenContract.TOKENSTATE_ID, tokenState);
            tx.command(alice.getPublicKey(), new TokenContract.Commands.Issue());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has no input, will verify.
            tx.output(TokenContract.TOKENSTATE_ID, tokenState);
            tx.command(alice.getPublicKey(), new TokenContract.Commands.Issue());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void tokenContractRequiresOneOutputInTheTransaction() {
        transaction(ledgerServices, tx -> {
            // Has two outputs, will fail.
            tx.output(TokenContract.TOKENSTATE_ID, tokenState);
            tx.output(TokenContract.TOKENSTATE_ID, tokenState);
            tx.command(alice.getPublicKey(), new TokenContract.Commands.Issue());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has one output, will verify.
            tx.output(TokenContract.TOKENSTATE_ID, tokenState);
            tx.command(alice.getPublicKey(), new TokenContract.Commands.Issue());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void tokenContractRequiresOneCommandInTheTransaction() {
        transaction(ledgerServices, tx -> {
            tx.output(TokenContract.TOKENSTATE_ID, tokenState);
            // Has two commands, will fail.
            tx.command(alice.getPublicKey(), new TokenContract.Commands.Issue());
            tx.command(alice.getPublicKey(), new TokenContract.Commands.Issue());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            tx.output(TokenContract.TOKENSTATE_ID, tokenState);
            // Has one command, will verify.
            tx.command(alice.getPublicKey(), new TokenContract.Commands.Issue());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void tokenContractRequiresTheTransactionsOutputToBeATokenState() {
        transaction(ledgerServices, tx -> {
            // Has wrong output type, will fail.
            tx.output(TokenContract.TOKENSTATE_ID, new DummyState());
            tx.command(alice.getPublicKey(), new TokenContract.Commands.Issue());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has correct output type, will verify.
            tx.output(TokenContract.TOKENSTATE_ID, tokenState);
            tx.command(alice.getPublicKey(), new TokenContract.Commands.Issue());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void tokenContractRequiresTheTransactionsOutputToHaveAPositiveAmount() {
        TokenState zeroTokenState = new TokenState(alice.getParty(), bob.getParty(), -1);
        TokenState negativeTokenState = new TokenState(alice.getParty(), bob.getParty(), -1);
        TokenState positiveTokenState = new TokenState(alice.getParty(), bob.getParty(), 2);

        transaction(ledgerServices, tx -> {
            // Has zero-amount TokenState, will fail.
            tx.output(TokenContract.TOKENSTATE_ID, zeroTokenState);
            tx.command(alice.getPublicKey(), new TokenContract.Commands.Issue());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has negative-amount TokenState, will fail.
            tx.output(TokenContract.TOKENSTATE_ID, negativeTokenState);
            tx.command(alice.getPublicKey(), new TokenContract.Commands.Issue());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has positive-amount TokenState, will verify.
            tx.output(TokenContract.TOKENSTATE_ID, tokenState);
            tx.command(alice.getPublicKey(), new TokenContract.Commands.Issue());
            tx.verifies();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Also has positive-amount TokenState, will verify.
            tx.output(TokenContract.TOKENSTATE_ID, positiveTokenState);
            tx.command(alice.getPublicKey(), new TokenContract.Commands.Issue());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void tokenContractRequiresTheTransactionsCommandToBeAnIssueCommand() {
        transaction(ledgerServices, tx -> {
            // Has wrong command type, will fail.
            tx.output(TokenContract.TOKENSTATE_ID, tokenState);
            tx.command(alice.getPublicKey(), DummyCommandData.INSTANCE);
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Has correct command type, will verify.
            tx.output(TokenContract.TOKENSTATE_ID, tokenState);
            tx.command(alice.getPublicKey(), new TokenContract.Commands.Issue());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void tokenContractRequiresTheIssuerToBeARequiredSignerInTheTransaction() {
        TokenState tokenStateWhereBobIsIssuer = new TokenState(bob.getParty(), alice.getParty(), 1);

        transaction(ledgerServices, tx -> {
            // Issuer is not a required signer, will fail.
            tx.output(TokenContract.TOKENSTATE_ID, tokenState);
            tx.command(bob.getPublicKey(), new TokenContract.Commands.Issue());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Issuer is also not a required signer, will fail.
            tx.output(TokenContract.TOKENSTATE_ID, tokenStateWhereBobIsIssuer);
            tx.command(alice.getPublicKey(), new TokenContract.Commands.Issue());
            tx.fails();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Issuer is a required signer, will verify.
            tx.output(TokenContract.TOKENSTATE_ID, tokenState);
            tx.command(alice.getPublicKey(), new TokenContract.Commands.Issue());
            tx.verifies();
            return null;
        });

        transaction(ledgerServices, tx -> {
            // Issuer is also a required signer, will verify.
            tx.output(TokenContract.TOKENSTATE_ID, tokenStateWhereBobIsIssuer);
            tx.command(bob.getPublicKey(), new TokenContract.Commands.Issue());
            tx.verifies();
            return null;
        });
    }

}