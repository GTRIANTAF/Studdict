package com.studdict;

import com.studdict.model.MenuItem;
import com.studdict.repository.MenuItemRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Runs on startup to ensure menu_items have is_available = true.
 *
 * Two cases handled:
 *   1. Table is empty → seeds 6 demo items.
 *   2. Items exist with is_available = false (Java boolean default when inserted
 *      without setting the field) → fixes them so orders can go through.
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private final MenuItemRepository menuItemRepository;

    public DataInitializer(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<MenuItem> existing = menuItemRepository.findAll();

        if (existing.isEmpty()) {
            seedMenuItems();
        } else {
            fixUnavailableItems(existing);
        }
    }

    private void fixUnavailableItems(List<MenuItem> items) {
        boolean anyFixed = false;
        for (MenuItem item : items) {
            if (!item.isAvailable()) {
                item.setAvailable(true);
                menuItemRepository.save(item);
                anyFixed = true;
            }
        }
        if (anyFixed) {
            System.out.println("[DataInitializer] Fixed menu items with is_available=false → true.");
        }
    }

    private void seedMenuItems() {
        Object[][] data = {
            {"Espresso",       2.50, "Coffee"},
            {"Cappuccino",     3.20, "Coffee"},
            {"Club Sandwich",  5.50, "Food"},
            {"Croissant",      2.00, "Food"},
            {"Orange Juice",   3.00, "Drinks"},
            {"Mineral Water",  1.50, "Drinks"},
        };
        for (Object[] d : data) {
            MenuItem item = new MenuItem();
            item.setName((String) d[0]);
            item.setPrice((Double) d[1]);
            item.setCategory((String) d[2]);
            item.setAvailable(true);
            menuItemRepository.save(item);
        }
        System.out.println("[DataInitializer] Seeded 6 demo menu items.");
    }
}
