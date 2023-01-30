package uk.openvk.android.refresh.user_interface.list_items;

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
