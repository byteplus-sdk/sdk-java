package byteplus.sdk.core.metrics;

import lombok.Getter;

public class Item<T> {
    @Getter
    private final String tags;
    @Getter
    private final T value;

    public Item(String tags, T value) {
        this.tags = tags;
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (obj instanceof Item) {
            Item item = (Item) obj;
            return this.getTags().equals(item.getTags());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return tags.hashCode();
    }

    @Override
    public String toString() {
        return "Item{" +
                "tags='" + tags + '\'' +
                ", value=" + value +
                '}';
    }
}
