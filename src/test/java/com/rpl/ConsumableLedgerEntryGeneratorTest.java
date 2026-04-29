package com.rpl;

import com.rpl.domain.*;
import com.rpl.domain.composite.ActionStatus;
import com.rpl.domain.ledger.ConsumableLedgerEntryGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class ConsumableLedgerEntryGeneratorTest {

    private ConsumableLedgerEntryGenerator generator;
    private ResourceType consumableType;
    private ResourceType assetType;
    private Account poolAccount;

    @BeforeEach
    void setUp() {
        generator = new ConsumableLedgerEntryGenerator();

       
        consumableType = new ResourceType("Concrete", ResourceType.Kind.CONSUMABLE, "m3");
        poolAccount = new Account("Concrete Pool", Account.Kind.POOL, consumableType);
        consumableType.setPoolAccount(poolAccount);

      
        assetType = new ResourceType("Crane", ResourceType.Kind.ASSET, "units");
        Account assetPool = new Account("Crane Pool", Account.Kind.POOL, assetType);
        assetType.setPoolAccount(assetPool);
    }

    private ImplementedAction buildImplementedAction(List<ResourceAllocation> allocs) {
        ProposedAction pa = new ProposedAction("Build Foundation");
        pa.setStatusEnum(ActionStatus.IN_PROGRESS);
        for (ResourceAllocation a : allocs) {
            a.setAction(pa);
            pa.getAllocations().add(a);
        }
        ImplementedAction ia = new ImplementedAction(pa);
        return ia;
    }

    @Test
    void generateEntries_singleConsumable_producesBalancedTransaction() {
       
        ResourceAllocation alloc = new ResourceAllocation(consumableType, new BigDecimal("10.00"), ResourceAllocation.AllocationKind.GENERAL);
        ImplementedAction ia = buildImplementedAction(List.of(alloc));
       
        Transaction tx = generator.generateEntries(ia);
      
        assertEquals(2, tx.getEntries().size());
        BigDecimal net = tx.getEntries().stream()
            .map(Entry::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, net.compareTo(BigDecimal.ZERO), "Double-entry must net to zero");
    }

    @Test
    void generateEntries_filtersOutAssetAllocations() {
        
        ResourceAllocation consumAlloc = new ResourceAllocation(consumableType, new BigDecimal("5"), ResourceAllocation.AllocationKind.GENERAL);
        ResourceAllocation assetAlloc  = new ResourceAllocation(assetType, new BigDecimal("1"), ResourceAllocation.AllocationKind.SPECIFIC);
        ImplementedAction ia = buildImplementedAction(List.of(consumAlloc, assetAlloc));
       
        Transaction tx = generator.generateEntries(ia);
       
        assertEquals(2, tx.getEntries().size());
    }

    @Test
    void generateEntries_noConsumableAllocations_producesEmptyTransaction() {
    
        ResourceAllocation assetAlloc = new ResourceAllocation(assetType, new BigDecimal("2"), ResourceAllocation.AllocationKind.GENERAL);
        ImplementedAction ia = buildImplementedAction(List.of(assetAlloc));

        Transaction tx = generator.generateEntries(ia);

        assertEquals(0, tx.getEntries().size());
    }

    @Test
    void generateEntries_withdrawalIsNegative_depositIsPositive() {
 
        ResourceAllocation alloc = new ResourceAllocation(consumableType, new BigDecimal("7.5"), ResourceAllocation.AllocationKind.GENERAL);
        ImplementedAction ia = buildImplementedAction(List.of(alloc));
 
        Transaction tx = generator.generateEntries(ia);
  
        Entry withdrawal = tx.getEntries().stream()
            .filter(e -> e.getEntryType() == Entry.EntryType.WITHDRAWAL).findFirst().orElseThrow();
        Entry deposit = tx.getEntries().stream()
            .filter(e -> e.getEntryType() == Entry.EntryType.DEPOSIT).findFirst().orElseThrow();
        assertTrue(withdrawal.getAmount().compareTo(BigDecimal.ZERO) < 0, "Withdrawal must be negative");
        assertTrue(deposit.getAmount().compareTo(BigDecimal.ZERO) > 0, "Deposit must be positive");
    }

    @Test
    void validate_zeroQuantity_throwsIllegalArgument() {
       
        ResourceAllocation alloc = new ResourceAllocation(consumableType, BigDecimal.ZERO, ResourceAllocation.AllocationKind.GENERAL);
        ImplementedAction ia = buildImplementedAction(List.of(alloc));
     
        assertThrows(IllegalArgumentException.class, () -> generator.generateEntries(ia));
    }

    @Test
    void validate_negativeQuantity_throwsIllegalArgument() {
       
        ResourceAllocation alloc = new ResourceAllocation(consumableType, new BigDecimal("-5"), ResourceAllocation.AllocationKind.GENERAL);
        ImplementedAction ia = buildImplementedAction(List.of(alloc));

        assertThrows(IllegalArgumentException.class, () -> generator.generateEntries(ia));
    }

    @Test
    void generateEntries_multipleConsumables_eachProducesTwoEntries() {
      
        ResourceType lumber = new ResourceType("Lumber", ResourceType.Kind.CONSUMABLE, "board-ft");
        Account lumberPool = new Account("Lumber Pool", Account.Kind.POOL, lumber);
        lumber.setPoolAccount(lumberPool);

        ResourceAllocation alloc1 = new ResourceAllocation(consumableType, new BigDecimal("10"), ResourceAllocation.AllocationKind.GENERAL);
        ResourceAllocation alloc2 = new ResourceAllocation(lumber, new BigDecimal("20"), ResourceAllocation.AllocationKind.GENERAL);
        ImplementedAction ia = buildImplementedAction(List.of(alloc1, alloc2));
        
        Transaction tx = generator.generateEntries(ia);
      
        assertEquals(4, tx.getEntries().size());
    }

    @Test
    void withdrawal_linkedToCorrectPoolAccount() {
        
        ResourceAllocation alloc = new ResourceAllocation(consumableType, new BigDecimal("3"), ResourceAllocation.AllocationKind.GENERAL);
        ImplementedAction ia = buildImplementedAction(List.of(alloc));
       
        Transaction tx = generator.generateEntries(ia);
       
        Entry withdrawal = tx.getEntries().stream()
            .filter(e -> e.getEntryType() == Entry.EntryType.WITHDRAWAL).findFirst().orElseThrow();
        assertEquals(poolAccount.getName(), withdrawal.getAccount().getName());
    }
}
