package java_example;

import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.util.List;

// Like all states, implements `ContractState`.
public class LandState implements ContractState {
    // The attributes that will stored on the ledger as part of state.
    private final Party owner;
    private final Party buyer;
    private final String landId;
    private final String landLocation;
    private final int landArea;
    private final int price;

    // The constructor used to create an instance of the state.
    public LandState(Party owner, Party buyer, String landId, String landLocation, int landArea, int price) {
        this.owner = owner;
        this.buyer = buyer;
        this.landId = landId;
        this.landLocation = landLocation;
        this.landArea = landArea;
        this.price = price;
    }

    // Overrides `participants`, the only field defined by `ContractState`.
    // Defines which parties will store the state.
    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return ImmutableList.of(owner, buyer);
    }

    public Party getOwner() { return owner; }

    public Party getBuyer() { return buyer; }

    public String getLandId() { return landId; }

    public String getLandLocation() { return landLocation; }

    public int getLandArea() { return landArea; }

    public int getPrice() { return price; }
}
