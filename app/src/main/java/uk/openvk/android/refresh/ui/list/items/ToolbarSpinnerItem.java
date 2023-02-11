package uk.openvk.android.refresh.ui.list.items;

public class ToolbarSpinnerItem {
    public String name;
    public String category;
    public long id;

    public ToolbarSpinnerItem(String category, String name, long id) {
        this.category = category;
        this.name = name;
        this.id = id;
    }
}
