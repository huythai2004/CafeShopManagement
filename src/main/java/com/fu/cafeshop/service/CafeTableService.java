package com.fu.cafeshop.service;

import com.fu.cafeshop.entity.CafeTable;
import com.fu.cafeshop.repository.CafeTableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CafeTableService {

    private final CafeTableRepository cafeTableRepository;

    private static final int DEFAULT_TABLE_COUNT = 20;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initDefaultTables() {
        try {
            // Only initialize if no tables exist
            if (cafeTableRepository.count() == 0) {
                log.info("Initializing {} default tables...", DEFAULT_TABLE_COUNT);
                for (int i = 1; i <= DEFAULT_TABLE_COUNT; i++) {
                    CafeTable table = CafeTable.builder()
                            .tableNumber(i)
                            .name("Bàn " + i)
                            .capacity(4)
                            .isActive(true)
                            .build();
                    cafeTableRepository.save(table);
                }
                log.info("Default tables initialized successfully");
            }
        } catch (Exception e) {
            log.warn("Could not initialize default tables. Please ensure the cafe_tables table exists in the database. Error: {}", e.getMessage());
        }
    }

    public List<CafeTable> getAllTables() {
        return cafeTableRepository.findAllByOrderByTableNumberAsc();
    }

    public List<CafeTable> getActiveTables() {
        return cafeTableRepository.findByIsActiveTrueOrderByTableNumberAsc();
    }

    public CafeTable getTableById(Long id) {
        return cafeTableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn với id: " + id));
    }

    public CafeTable getTableByNumber(Integer tableNumber) {
        return cafeTableRepository.findByTableNumber(tableNumber)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn số: " + tableNumber));
    }

    @Transactional
    public CafeTable createTable(CafeTable table) {
        if (cafeTableRepository.existsByTableNumber(table.getTableNumber())) {
            throw new RuntimeException("Số bàn " + table.getTableNumber() + " đã tồn tại!");
        }
        return cafeTableRepository.save(table);
    }

    @Transactional
    public CafeTable updateTable(Long id, CafeTable tableDetails) {
        CafeTable table = getTableById(id);
        
        // Check if new table number conflicts with existing
        if (!table.getTableNumber().equals(tableDetails.getTableNumber()) 
                && cafeTableRepository.existsByTableNumber(tableDetails.getTableNumber())) {
            throw new RuntimeException("Số bàn " + tableDetails.getTableNumber() + " đã tồn tại!");
        }
        
        table.setTableNumber(tableDetails.getTableNumber());
        table.setName(tableDetails.getName());
        table.setCapacity(tableDetails.getCapacity());
        table.setIsActive(tableDetails.getIsActive());
        
        return cafeTableRepository.save(table);
    }

    @Transactional
    public void deleteTable(Long id) {
        CafeTable table = getTableById(id);
        cafeTableRepository.delete(table);
    }

    @Transactional
    public void toggleTableStatus(Long id) {
        CafeTable table = getTableById(id);
        table.setIsActive(!table.getIsActive());
        cafeTableRepository.save(table);
    }

    public long countTables() {
        return cafeTableRepository.count();
    }

    public long countActiveTables() {
        return cafeTableRepository.findByIsActiveTrueOrderByTableNumberAsc().size();
    }
}


