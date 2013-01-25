package kz.bsbnb.usci.eav.persistance;

public class Persistable {
	/**
	 * id fields value of the persisted object
	 */
	protected long id = 0;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
}
