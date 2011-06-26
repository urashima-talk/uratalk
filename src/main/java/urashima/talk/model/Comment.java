package urashima.talk.model;

import java.io.Serializable;
import java.util.Date;

import com.google.appengine.api.datastore.Key;

import org.slim3.datastore.Attribute;
import org.slim3.datastore.Model;
import org.slim3.datastore.ModelRef;

@Model(kind = "c", schemaVersion = 1, schemaVersionName = "sV")
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;

    @Attribute(name="k", primaryKey = true)
    private Key key;

    @Attribute(name="v", version = true)
    private Long version;

    @Attribute(name="iN")
    private boolean isNoticed;
    
    @Attribute(name="iH")
    private boolean isHidden;
    
    @Attribute(name="n")
    private String name;
    
    @Attribute(name="t")
    private String title;
    
    @Attribute(name="c", lob=true)
    private String content;
    
    @Attribute(name="rK")
    private String referenceKey;
    
    @Attribute(name="cA")
    private Date createdAt;
    
    @Attribute(name="nS")
    private String numberString;
    
    @Attribute(name="tR")
    private ModelRef<Topic> topicRef = new ModelRef<Topic>(Topic.class);
    
    /**
     * Returns the key.
     *
     * @return the key
     */
    public Key getKey() {
        return key;
    }

    /**
     * Sets the key.
     *
     * @param key
     *            the key
     */
    public void setKey(Key key) {
        this.key = key;
    }

    /**
     * Returns the version.
     *
     * @return the version
     */
    public Long getVersion() {
        return version;
    }

    /**
     * Sets the version.
     *
     * @param version
     *            the version
     */
    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Comment other = (Comment) obj;
        if (key == null) {
            if (other.key != null) {
                return false;
            }
        } else if (!key.equals(other.key)) {
            return false;
        }
        return true;
    }
    
	public void setNoticed(boolean isNoticed) {
		this.isNoticed = isNoticed;
	}

	public boolean isNoticed() {
		return isNoticed;
	}

	public void setHidden(boolean isHidden) {
		this.isHidden = isHidden;
	}

	public boolean isHidden() {
		return isHidden;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getContent() {
		return content;
	}

	public void setReferenceKey(String referenceKey) {
		this.referenceKey = referenceKey;
	}

	public String getReferenceKey() {
		return referenceKey;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public ModelRef<Topic> getTopicRef() {
		return topicRef;
	}

	public void setNumberString(String numberString) {
		this.numberString = numberString;
	}

	public String getNumberString() {
		return numberString;
	}
}
