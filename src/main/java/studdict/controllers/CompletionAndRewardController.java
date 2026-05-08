package studdict.controllers;

import studdict.core.Table;

public class CompletionAndRewardController {
    public void finalizeCheckout() {
        Table table = new Table();
        table.freeTable();

        System.out.println("Point credited to Loyalty wallet");
    }
}
