package urashima.talk.model;

import java.io.Serializable;

import com.google.appengine.api.datastore.Key;

import org.slim3.datastore.Attribute;
import org.slim3.datastore.Model;
import org.slim3.datastore.ModelRef;

@Model(kind = "nC", schemaVersion = 1, schemaVersionName = "sV")
public class NoticeComment implements Serializable {

	private static final long serialVersionUID = 1L;

	@Attribute(name = "k", primaryKey = true)
	private Key key;

	@Attribute(name = "v", version = true)
	private Long version;

	@Attribute(name = "cR")
	private ModelRef<Comment> commentRef = new ModelRef<Comment>(Comment.class);

	@Attribute(name = "rK")
	private String referenceKey;

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
		NoticeComment other = (NoticeComment) obj;
		if (key == null) {
			if (other.key != null) {
				return false;
			}
		} else if (!key.equals(other.key)) {
			return false;
		}
		return true;
	}

	public String getReferenceKey() {
		return referenceKey;
	}

	public void setCommentRef(ModelRef<Comment> commentRef) {
		this.commentRef = commentRef;
	}

	public ModelRef<Comment> getCommentRef() {
		return commentRef;
	}
}
