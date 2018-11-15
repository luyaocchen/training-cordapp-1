package java_example;

import net.corda.core.contracts.ContractState;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.testing.core.TestIdentity;
import org.junit.Test;

import static org.junit.Assert.*;

public class LandStateTests {
    private final Party alice = new TestIdentity(new CordaX500Name("Alice", "", "DE")).getParty();
    private final Party bob = new TestIdentity(new CordaX500Name("Bob", "", "DE")).getParty();
    private final String landId = "F123456";
    private final String landLocation = "Frankfurt";

    @Test
    public void landStateHasOwnerBuyerLandidLandlocationAndPriceParamsOfCorrectTypeInConstructor() {
        new LandState(alice, bob, landId, landLocation, 1, 1);
    }

    @Test
    public void landStateHasGettersOwnerBuyerLandidLandlocationAndPrice() {
        LandState landState = new LandState(alice, bob, landId, landLocation, 1, 1);
        assertEquals(alice, landState.getOwner());
        assertEquals(bob, landState.getBuyer());
        assertEquals(landId, landState.getLandId());
        assertEquals(landLocation, landState.getLandLocation());
        assertEquals(1, landState.getLandArea());
        assertEquals(1, landState.getPrice());
    }

    @Test
    public void landStateImplementsContractState() {
        assert (new LandState(alice, bob, landId, landLocation, 1, 1) instanceof ContractState);
    }

    @Test
    public void landStateHasTwoPartcipantsTheOwnerAndTheBuyer() {
        LandState landState = new LandState(alice, bob, landId, landLocation, 1, 1);
        assertEquals(2, landState.getParticipants().size());
        assert(landState.getParticipants().contains(alice));
        assert(landState.getParticipants().contains(bob));
    }
}