package daris.client.model.dicom;

public class AttributeTag {
    public final int group;
    public final int element;

    public AttributeTag(int group, int element) {
        this.group = group;
        this.element = element;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof AttributeTag) {
            AttributeTag ato = (AttributeTag) o;
            return ato.group == this.group && ato.element == this.element;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (this.group << 16) + (this.element & 0xffff);
    }

    @Override
    public String toString() {
        // NOTE: cannot using String.format() as it is not available in GWT java
        // emulation.
        StringBuilder sb = new StringBuilder();
        sb.append("(0x");
        sb.append(Integer.toHexString(0x10000 | this.group).substring(1));
        sb.append(",0x");
        sb.append(Integer.toHexString(0x10000 | this.element).substring(1));
        sb.append(")");
        return sb.toString();
    }
}
