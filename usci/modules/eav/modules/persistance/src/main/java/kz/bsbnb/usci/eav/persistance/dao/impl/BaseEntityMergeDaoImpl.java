package kz.bsbnb.usci.eav.persistance.dao.impl;

import kz.bsbnb.usci.eav.manager.IBaseEntityManager;
import kz.bsbnb.usci.eav.manager.IBaseEntityMergeManager;
import kz.bsbnb.usci.eav.manager.impl.BaseEntityManager;
import kz.bsbnb.usci.eav.manager.impl.MergeManagerKey;
import kz.bsbnb.usci.eav.model.base.IBaseContainer;
import kz.bsbnb.usci.eav.model.base.IBaseEntity;
import kz.bsbnb.usci.eav.model.base.IBaseSet;
import kz.bsbnb.usci.eav.model.base.IBaseValue;
import kz.bsbnb.usci.eav.model.base.impl.BaseEntity;
import kz.bsbnb.usci.eav.model.base.impl.BaseSet;
import kz.bsbnb.usci.eav.model.base.impl.BaseValueFactory;
import kz.bsbnb.usci.eav.model.base.impl.OperationType;
import kz.bsbnb.usci.eav.model.meta.IMetaAttribute;
import kz.bsbnb.usci.eav.model.meta.IMetaClass;
import kz.bsbnb.usci.eav.model.meta.IMetaSet;
import kz.bsbnb.usci.eav.model.meta.IMetaType;
import kz.bsbnb.usci.eav.model.meta.impl.MetaContainerTypes;
import kz.bsbnb.usci.eav.model.persistable.IPersistable;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityApplyDao;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityDao;
import kz.bsbnb.usci.eav.persistance.dao.IBaseEntityMergeDao;
import kz.bsbnb.usci.eav.persistance.dao.listener.IDaoListener;
import kz.bsbnb.usci.eav.persistance.dao.pool.IPersistableDaoPool;
import kz.bsbnb.usci.eav.util.Errors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class BaseEntityMergeDaoImpl implements IBaseEntityMergeDao {
	@Autowired
	IPersistableDaoPool persistableDaoPool;

	@Autowired
	IBaseEntityApplyDao baseEntityApplyDao;

	IDaoListener applyListener;

	@Autowired
	public void setApplyListener(IDaoListener applyListener) {
		this.applyListener = applyListener;
	}

	/**
	 * Given two entities, merge manager. and merge result choice, perform merge
	 * operation and return resulting base entity. The choice of the resulting entity
	 * (right or left) depends on merge result choice. Changes are reflected in the database
	 *
	 * @param baseEntityLeft  - left entity
	 * @param baseEntityRight - right entity
	 * @param mergeManager    - merge manager containing information about how the two entities
	 *                        are to be merged
	 * @param choice          - MergeResultChoice object - determines the resulting entity
	 * @return IBaseEntity containing the result of the merge operation. Depending on
	 * choice it is either left or right entity
	 */
	public IBaseEntity merge(IBaseEntity baseEntityLeft, IBaseEntity baseEntityRight,
							 IBaseEntityMergeManager mergeManager, MergeResultChoice choice, boolean deleteUnused) {
		IBaseEntityManager baseEntityManager = new BaseEntityManager();
		IBaseEntity resultingBaseEntity = mergeBaseEntity(baseEntityLeft, baseEntityRight,
				mergeManager, baseEntityManager, choice, deleteUnused);

		baseEntityApplyDao.applyToDb(baseEntityManager);

		applyListener.applyToDBEnded(null, baseEntityRight, resultingBaseEntity, baseEntityManager);


		return resultingBaseEntity;
	}

	/**
	 * @param baseEntityLeft    - first base entity
	 * @param baseEntityRight   -  second base entity
	 * @param mergeManager      -  - merge manager containing information about how the two entities
	 *                          are to be merged
	 * @param baseEntityManager - base entity manager which will hold all the changes to database resulting
	 *                          from the merge operation
	 * @param choice            - MergeResultChoice object - determines the resulting entity
	 * @param deleteUnused
	 * @return IBaseEntity containing the result of the merge operation. Depending on
	 * choice it is either left or right entity
	 * @author dakkuliyev
	 * Given right and left entities, merge manager object, base entity manager object,
	 * and merge result choice perform merge operation. Does not perform any operations in
	 * the database.
	 */
	private IBaseEntity mergeBaseEntity(IBaseEntity baseEntityLeft, IBaseEntity baseEntityRight, IBaseEntityMergeManager mergeManager,
										IBaseEntityManager baseEntityManager, MergeResultChoice choice, boolean deleteUnused) {

		long creditorId = 0;
		if (baseEntityLeft.getMeta().getClassName().equals("credit")) {
			long creditorIdLeft = baseEntityLeft.getBaseEntityReportDate().getCreditorId();
			long creditorIdRight = baseEntityRight.getBaseEntityReportDate().getCreditorId();
			if (creditorIdLeft != creditorIdRight)
				throw new IllegalStateException(String.valueOf(Errors.E104));

			creditorId = creditorIdLeft;
		}

		// although it is safe to assume that both entities exist in DB, it is still worth checking
		if (baseEntityLeft.getId() < 1 && baseEntityRight.getId() < 1) {
			throw new RuntimeException(String.valueOf(Errors.E105));
		}

		IMetaClass metaClass = baseEntityLeft.getMeta();
		IBaseEntity baseEntityApplied;


		if (choice == MergeResultChoice.RIGHT) {
			baseEntityApplied = new BaseEntity(baseEntityRight, baseEntityRight.getReportDate());
		} else {
			baseEntityApplied = new BaseEntity(baseEntityLeft, baseEntityLeft.getReportDate());
		}

		for (String attribute : metaClass.getAttributeNames()) {
			IBaseValue baseValueLeft = baseEntityLeft.getBaseValue(attribute);
			IBaseValue baseValueRight = baseEntityRight.getBaseValue(attribute);
			MergeManagerKey<String> attrKey = new MergeManagerKey<String>(attribute);

			if (baseValueLeft != null && baseValueRight != null) {
				IMetaAttribute metaAttribute = baseValueLeft.getMetaAttribute();
				IMetaType metaType = metaAttribute.getMetaType();
				// since there is no child map - there is need to look for child merge manager
				if (mergeManager.getChildMap() == null) {
					if (metaType.isSetOfSets()) {
						throw new UnsupportedOperationException(String.valueOf(Errors.E2));
					}

					if (metaType.isSet()) {
						// merge set
						mergeSet(creditorId, baseEntityApplied, baseValueLeft, baseValueRight,
								mergeManager, baseEntityManager, choice, deleteUnused);
					} else {
						// merge value
						mergeValue(creditorId, baseEntityApplied, baseValueLeft, baseValueRight,
								mergeManager, baseEntityManager, choice, deleteUnused);
					}
				} else {
					// get child manager for this attribute
					if (mergeManager.containsKey(attrKey)) {

						if (metaType.isSetOfSets()) {
							throw new UnsupportedOperationException(String.valueOf(Errors.E2));
						}

						if (metaType.isSet()) {
							// merge set
							for (IBaseEntityMergeManager baseEntityMergeManager : mergeManager.getChildManager(attrKey)) {
								mergeSet(creditorId, baseEntityApplied, baseValueLeft, baseValueRight,
										baseEntityMergeManager, baseEntityManager, choice, deleteUnused);
							}
						} else {
							// merge value
							for (IBaseEntityMergeManager baseEntityMergeManager : mergeManager.getChildManager(attrKey)) {
								mergeValue(creditorId, baseEntityApplied, baseValueLeft, baseValueRight,
										baseEntityMergeManager, baseEntityManager, choice, deleteUnused);
							}
						}

					} else {
						// not present in merge manager - hence just copy to baseEntityApplied
						if (choice == MergeResultChoice.RIGHT) {
							IBaseValue baseValueRightApplied = BaseValueFactory.create(
									MetaContainerTypes.META_CLASS,
									metaType,
									baseValueRight.getId(),
									creditorId,
									new Date(baseValueRight.getRepDate().getTime()),
									baseValueRight.getValue(),
									baseValueRight.isClosed(),
									baseValueRight.isLast());
							baseEntityApplied.put(metaAttribute.getName(), baseValueRightApplied);
						} else {
							IBaseValue baseValueLeftApplied = BaseValueFactory.create(
									MetaContainerTypes.META_CLASS,
									metaType,
									baseValueLeft.getId(),
									creditorId,
									new Date(baseValueLeft.getRepDate().getTime()),
									baseValueLeft.getValue(),
									baseValueLeft.isClosed(),
									baseValueLeft.isLast());
							baseEntityApplied.put(metaAttribute.getName(), baseValueLeftApplied);
						}

					}

				}

			} else {
				// if both of the values are null - do nothing
				if (baseValueLeft == null && baseValueRight == null) {
					continue;
				} else {
					if (mergeManager.containsKey(attrKey)) {
						// one of the basevalues is null and there is a child manager
						for (IBaseEntityMergeManager baseEntityMergeManager : mergeManager.getChildManager(attrKey)) {
							mergeNullValue(baseEntityApplied, baseEntityLeft, baseEntityRight, baseEntityMergeManager,
									baseEntityManager, choice, attribute);
						}
					} else {
						// one of the basevalues is null and there is no child manager
						if (choice == MergeResultChoice.RIGHT) {
							if (baseValueRight != null)
								baseEntityApplied.put(baseEntityApplied.isSet() ? null : attribute, baseValueRight);
						} else {
							if (baseValueLeft != null)
								baseEntityApplied.put(baseEntityApplied.isSet() ? null : attribute, baseValueLeft);
						}
					}
				}
			}
		}

		return baseEntityApplied;
	}

	/**
	 * @param baseEntity        - baseEntity resulting from the merge operation
	 * @param baseEntityLeft    - first base entity
	 * @param baseEntityRight   -  second base entity
	 * @param mergeManager      -  - merge manager containing information about how the two entities
	 *                          are to be merged
	 * @param baseEntityManager - base entity manager which will hold all the changes to database resulting
	 *                          from the merge operation
	 * @param choice            - MergeResultChoice object - determines the resulting entity
	 * @param attribute         - attribute of the null base value
	 * @author dakkuliyev
	 * Given right and left entities, merge manager object, base entity manager object,
	 * and merge result choice perform merge operation. Does not perform any operations in
	 * the database. The result of the opration is reflected in baseEntity. Method does not
	 * return anything.
	 * This method is used when one of entities has null base value
	 */
	private void mergeNullValue(IBaseEntity baseEntity, IBaseEntity baseEntityLeft, IBaseEntity baseEntityRight,
								IBaseEntityMergeManager mergeManager,
								IBaseEntityManager baseEntityManager, MergeResultChoice choice, String attribute) {
		long creditorId = 0;
		if (baseEntityLeft.getMeta().getClassName().equals("credit")) {
			long creditorIdLeft = baseEntityLeft.getBaseEntityReportDate().getCreditorId();
			long creditorIdRight = baseEntityRight.getBaseEntityReportDate().getCreditorId();

			if (creditorIdLeft != creditorIdRight)
				throw new IllegalStateException(String.valueOf(Errors.E104));

			creditorId = creditorIdLeft;
		}

		IBaseValue baseValueLeft = baseEntityLeft.getBaseValue(attribute);
		IBaseValue baseValueRight = baseEntityRight.getBaseValue(attribute);
		if (baseValueLeft == null) {
			IMetaAttribute metaAttribute = baseValueRight.getMetaAttribute();
			IMetaType metaType = metaAttribute.getMetaType();
			if (mergeManager.getAction() == IBaseEntityMergeManager.Action.KEEP_LEFT) {
				IBaseValue oldBaseValueRight = BaseValueFactory.create(
						MetaContainerTypes.META_CLASS,
						metaType,
						baseValueRight.getId(),
						creditorId,
						new Date(baseValueRight.getRepDate().getTime()),
						baseValueRight.getValue(),
						baseValueRight.isClosed(),
						baseValueRight.isLast());

				if (choice == MergeResultChoice.RIGHT) {
					oldBaseValueRight.setBaseContainer(baseEntity);
				} else {
					oldBaseValueRight.setBaseContainer(baseEntityRight);
				}
				// delete old baseValueRight
				oldBaseValueRight.setMetaAttribute(metaAttribute);
				registerAsDeleted(baseEntityManager, metaType, oldBaseValueRight);
			}
			if (mergeManager.getAction() == IBaseEntityMergeManager.Action.KEEP_RIGHT ||
					mergeManager.getAction() == IBaseEntityMergeManager.Action.TO_MERGE) {
				// copy from right to left
				IBaseValue newBaseValueLeft = BaseValueFactory.create(
						MetaContainerTypes.META_CLASS,
						metaType,
						baseValueRight.getId(),
						creditorId,
						new Date(baseValueRight.getRepDate().getTime()),
						baseValueRight.getValue(),
						baseValueRight.isClosed(),
						baseValueRight.isLast());

				if (choice == MergeResultChoice.RIGHT) {
					baseEntity.put(attribute, baseValueRight);

					newBaseValueLeft.setBaseContainer(baseEntityLeft);
					newBaseValueLeft.setMetaAttribute(metaAttribute);
					baseEntityManager.registerAsInserted(newBaseValueLeft);
				} else {
					// insert newBaseValueLeft
					baseEntity.put(attribute, newBaseValueLeft);
					baseEntityManager.registerAsInserted(newBaseValueLeft);
				}
			}
		}

		if (baseValueRight == null) {
			IMetaAttribute metaAttribute = baseValueLeft.getMetaAttribute();
			IMetaType metaType = metaAttribute.getMetaType();
			if (mergeManager.getAction() == IBaseEntityMergeManager.Action.KEEP_RIGHT) {
				IBaseValue oldBaseValueLeft = BaseValueFactory.create(
						MetaContainerTypes.META_CLASS,
						metaType,
						baseValueLeft.getId(),
						creditorId,
						new Date(baseValueLeft.getRepDate().getTime()),
						baseValueLeft.getValue(),
						baseValueLeft.isClosed(),
						baseValueLeft.isLast());

				if (choice == MergeResultChoice.RIGHT) {
					oldBaseValueLeft.setBaseContainer(baseEntityLeft);
				} else {
					oldBaseValueLeft.setBaseContainer(baseEntity);
				}
				oldBaseValueLeft.setMetaAttribute(metaAttribute);
				registerAsDeleted(baseEntityManager, metaType, oldBaseValueLeft);
			}
			if (mergeManager.getAction() == IBaseEntityMergeManager.Action.KEEP_LEFT ||
					mergeManager.getAction() == IBaseEntityMergeManager.Action.TO_MERGE) {
				// copy from left to right
				IBaseValue newBaseValueRight = BaseValueFactory.create(
						MetaContainerTypes.META_CLASS,
						metaType,
						baseValueLeft.getId(),
						creditorId,
						new Date(baseValueLeft.getRepDate().getTime()),
						baseValueLeft.getValue(),
						baseValueLeft.isClosed(),
						baseValueLeft.isLast());
				if (choice == MergeResultChoice.LEFT) {
					baseEntity.put(attribute, baseValueLeft);

					newBaseValueRight.setBaseContainer(baseEntityRight);
					newBaseValueRight.setMetaAttribute(metaAttribute);
					baseEntityManager.registerAsInserted(newBaseValueRight);
				} else {
					// insert newBasevalueRight
					baseEntity.put(attribute, newBaseValueRight);
					baseEntityManager.registerAsInserted(newBaseValueRight);
				}
			}
		}
	}

	private void registerAsDeleted(IBaseEntityManager baseEntityManager, IMetaType metaType, IBaseValue baseValue) {
		if (!metaType.isReference())
			if (metaType.isSet())
				for (IPersistable persistable : new ArrayList<IPersistable>(((BaseSet) baseValue.getValue()).get())) {
					baseEntityManager.registerAsDeleted(persistable);
				}
			else
				baseEntityManager.registerAsDeleted((IPersistable) baseValue.getValue());
	}

	/**
	 * @param baseEntity        - baseEntity resulting from the merge operation
	 * @param baseValueLeft     - first base entity
	 * @param baseValueRight    -  second base entity
	 * @param mergeManager      -  - merge manager containing information about how the two entities
	 *                          are to be merged
	 * @param baseEntityManager - base entity manager which will hold all the changes to database resulting
	 *                          from the merge operation
	 * @param choice            - MergeResultChoice object - determines the resulting entity
	 * @param deleteUnused
	 * @author dakkuliyev
	 * Given right and left base values, merge manager object, base entity manager object,
	 * and merge result choice perform merge operation. Does not perform any operations in
	 * the database. The result of the opration is reflected in baseEntity. Method does not
	 * return anything.
	 * This method is used when merging SETs
	 */
	private void mergeSet(long creditorId, IBaseEntity baseEntity, IBaseValue baseValueLeft, IBaseValue baseValueRight,
						  IBaseEntityMergeManager mergeManager,
						  IBaseEntityManager baseEntityManager, MergeResultChoice choice, boolean deleteUnused) {

		IMetaAttribute metaAttribute = baseValueLeft.getMetaAttribute();
		IMetaType metaType = metaAttribute.getMetaType();

		IMetaSet childMetaSet = (IMetaSet) metaType;
		IMetaType childMetaType = childMetaSet.getMemberType();

		IBaseSet childBaseSetLeft = (IBaseSet) baseValueLeft.getValue();
		IBaseSet childBaseSetRight = (IBaseSet) baseValueRight.getValue();
		IBaseSet childBaseSetApplied = null;
		IBaseSet childBaseSetAppliedRight = null;
		IBaseSet childBaseSetAppliedLeft = null;

		if (mergeManager.getAction() != IBaseEntityMergeManager.Action.KEEP_BOTH) {
			if (mergeManager.getAction() == IBaseEntityMergeManager.Action.KEEP_LEFT) {
				IBaseValue newBaseValueRight = BaseValueFactory.create(
						MetaContainerTypes.META_CLASS,
						metaType,
						baseValueRight.getId(),
						creditorId,
						new Date(baseValueLeft.getRepDate().getTime()),
						baseValueLeft.getValue(),
						baseValueLeft.isClosed(),
						baseValueLeft.isLast());

				newBaseValueRight.setBaseContainer(baseValueRight.getBaseContainer());
				newBaseValueRight.setMetaAttribute(metaAttribute);

				baseEntityManager.registerAsUpdated(newBaseValueRight);
				//delete
				registerAsDeleted(baseEntityManager, metaType, baseValueRight);

				if (mergeManager.getChildMap() == null) {
					if (choice == MergeResultChoice.LEFT) {
						baseEntity.put(baseEntity.isSet() ? null : metaAttribute.getName(), baseValueLeft);
					} else {
						baseEntity.put(baseEntity.isSet() ? null : metaAttribute.getName(), newBaseValueRight);
					}
				}

			} else if (mergeManager.getAction() == IBaseEntityMergeManager.Action.KEEP_RIGHT) {
				IBaseValue newBaseValueLeft = BaseValueFactory.create(
						MetaContainerTypes.META_CLASS,
						metaType,
						baseValueLeft.getId(),
						creditorId,
						new Date(baseValueRight.getRepDate().getTime()),
						baseValueRight.getValue(),
						baseValueRight.isClosed(),
						baseValueRight.isLast());

				newBaseValueLeft.setBaseContainer(baseValueLeft.getBaseContainer());
				newBaseValueLeft.setMetaAttribute(metaAttribute);

				baseEntityManager.registerAsUpdated(newBaseValueLeft);
				// delete
				registerAsDeleted(baseEntityManager, metaType, baseValueLeft);
				if (mergeManager.getChildMap() == null) {
					if (choice == MergeResultChoice.LEFT) {
						baseEntity.put(baseEntity.isSet() ? null : metaAttribute.getName(), newBaseValueLeft);
					} else {
						baseEntity.put(baseEntity.isSet() ? null : metaAttribute.getName(), baseValueRight);
					}
				}

			} else if (mergeManager.getAction() == IBaseEntityMergeManager.Action.TO_MERGE) {
				IBaseValue newBaseValueLeft = BaseValueFactory.create(
						MetaContainerTypes.META_CLASS,
						metaType,
						baseValueLeft.getId(),
						creditorId,
						new Date(baseValueRight.getRepDate().getTime()),
						baseValueRight.getValue(),
						baseValueRight.isClosed(),
						baseValueRight.isLast());

				newBaseValueLeft.setBaseContainer(baseValueLeft.getBaseContainer());
				newBaseValueLeft.setMetaAttribute(metaAttribute);

				baseEntityManager.registerAsUpdated(newBaseValueLeft);

				childBaseSetApplied = (BaseSet) baseValueRight.getValue();

				// merge two sets
				for (IBaseValue childBaseValueLeft : childBaseSetLeft.get()) {
					boolean contains = false;
					for (IBaseValue appliedSetValue : childBaseSetApplied.get()) {
						if (childBaseValueLeft.equals(appliedSetValue)) {
							contains = true;
						}
					}
					// if value is not a duplicate - then put it to the resulting set
					if (!contains) {
						IBaseValue newChildBaseValueLeft = BaseValueFactory.create(
								MetaContainerTypes.META_SET,
								childMetaType,
								childBaseValueLeft.getId(),
								creditorId,
								new Date(childBaseValueLeft.getRepDate().getTime()),
								childBaseValueLeft.getValue(),
								childBaseValueLeft.isClosed(),
								childBaseValueLeft.isLast());

						newChildBaseValueLeft.setBaseContainer(childBaseSetApplied);

						baseEntityManager.registerAsUpdated(newChildBaseValueLeft);
					}
				}

				baseEntity.put(baseEntity.isSet() ? null : metaAttribute.getName(), newBaseValueLeft);
			}
		}

		// we haven't reached the base case
		if (mergeManager.getChildMap() != null) {
			if (mergeManager.getAction() == IBaseEntityMergeManager.Action.TO_MERGE) {
				throw new UnsupportedOperationException(String.valueOf(Errors.E106));
			}

			Set<UUID> processedUuidsLeft = new HashSet<UUID>();
			Set<UUID> processedUuidsRight = new HashSet<UUID>();

			childBaseSetAppliedLeft = new BaseSet(childBaseSetLeft.getId(), childMetaType);
			childBaseSetAppliedRight = new BaseSet(childBaseSetRight.getId(), childMetaType);
			// we haven't reached the base case - do recursion
			for (IBaseValue childBaseValueLeft : childBaseSetLeft.get()) {
				IBaseEntity childBaseEntityLeft = (IBaseEntity) childBaseValueLeft.getValue();

				for (IBaseValue childBaseValueRight : childBaseSetRight.get()) {
					IBaseEntity childBaseEntityRight = (IBaseEntity) childBaseValueRight.getValue();

					MergeManagerKey idKey = new MergeManagerKey(childBaseEntityLeft.getId(),
							childBaseEntityRight.getId());
					for (IBaseEntityMergeManager baseEntityMergeManager : mergeManager.getChildManager(idKey)) {
						if (mergeManager.containsKey(idKey) && (baseEntityManager != null)) {
							if (processedUuidsLeft.contains(childBaseValueLeft.getUuid()) ||
									processedUuidsRight.contains(childBaseValueRight.getUuid())) {
								throw new RuntimeException(String.valueOf(Errors.E107));
							} else {
								processedUuidsLeft.add(childBaseValueLeft.getUuid());
								processedUuidsRight.add(childBaseValueRight.getUuid());
							}

							for (IBaseEntityMergeManager baseEntityMergeManager1 : mergeManager.getChildManager(idKey)) {
								IBaseEntity currentEntity = mergeBaseEntity(childBaseEntityLeft, childBaseEntityRight, baseEntityMergeManager1,
										baseEntityManager, choice, deleteUnused);

								//if(mergeManager.getAction() == IBaseEntityMergeManager.Action.KEEP_BOTH){
								if (choice == MergeResultChoice.LEFT) {

									IBaseValue newChildBaseValueLeft = BaseValueFactory.create(
											MetaContainerTypes.META_CLASS,
											childMetaType,
											childBaseValueLeft.getId(),
											creditorId,
											new Date(childBaseValueLeft.getRepDate().getTime()),
											currentEntity,
											childBaseValueLeft.isClosed(),
											childBaseValueLeft.isLast());
									childBaseSetAppliedLeft.put(newChildBaseValueLeft);

								} else {

									IBaseValue newChildBaseValueRight = BaseValueFactory.create(
											MetaContainerTypes.META_CLASS,
											childMetaType,
											childBaseValueRight.getId(),
											creditorId,
											new Date(childBaseValueRight.getRepDate().getTime()),
											currentEntity,
											childBaseValueRight.isClosed(),
											childBaseValueRight.isLast());
									childBaseSetAppliedRight.put(newChildBaseValueRight);
								}
							}
							//}
						}
					}
				}

			}
			// if(mergeManager.getAction() == IBaseEntityMergeManager.Action.KEEP_BOTH){
			if (choice == MergeResultChoice.LEFT) {
				IBaseValue newBaseValueLeft = BaseValueFactory.create(
						MetaContainerTypes.META_CLASS,
						metaType,
						baseValueLeft.getId(),
						creditorId,
						new Date(baseValueRight.getRepDate().getTime()),
						childBaseSetAppliedLeft,
						baseValueRight.isClosed(),
						baseValueRight.isLast());
				baseEntity.put(baseEntity.isSet() ? null : metaAttribute.getName(), newBaseValueLeft);
			} else {
				IBaseValue newBaseValueRight = BaseValueFactory.create(
						MetaContainerTypes.META_CLASS,
						metaType,
						baseValueRight.getId(),
						creditorId,
						new Date(baseValueLeft.getRepDate().getTime()),
						childBaseSetAppliedRight,
						baseValueLeft.isClosed(),
						baseValueLeft.isLast());
				baseEntity.put(baseEntity.isSet() ? null : metaAttribute.getName(), newBaseValueRight);
			}
			//}
		}
	}

	/**
	 * @param baseEntity        - baseEntity resulting from the merge operation
	 * @param baseValueLeft     - first base entity
	 * @param baseValueRight    -  second base entity
	 * @param mergeManager      -  - merge manager containing information about how the two entities
	 *                          are to be merged
	 * @param baseEntityManager - base entity manager which will hold all the changes to database resulting
	 *                          from the merge operation
	 * @param choice            - MergeResultChoice object - determines the resulting entity
	 * @param deleteUnused
	 * @author dakkuliyev
	 * Given right and left base values, merge manager object, base entity manager object,
	 * and merge result choice perform merge operation. Does not perform any operations in
	 * the database. The result of the opration is reflected in baseEntity. Method does not
	 * return anything.
	 * This method is used when merging Complex/Simple values
	 */
	private void mergeValue(long creditorId, IBaseContainer baseEntity, IBaseValue baseValueLeft, IBaseValue baseValueRight,
							IBaseEntityMergeManager mergeManager, IBaseEntityManager baseEntityManager,
							MergeResultChoice choice, boolean deleteUnused) {
		IMetaAttribute metaAttribute = baseValueLeft.getMetaAttribute();
		IMetaType metaType = metaAttribute.getMetaType();

		if (mergeManager.getAction() != IBaseEntityMergeManager.Action.KEEP_BOTH) {
			if (mergeManager.getAction() == IBaseEntityMergeManager.Action.KEEP_LEFT) {
				IBaseValue newBaseValueRight = BaseValueFactory.create(
						MetaContainerTypes.META_CLASS,
						metaType,
						baseValueRight.getId(),
						creditorId,
						new Date(baseValueLeft.getRepDate().getTime()),
						baseValueLeft.getValue(),
						baseValueLeft.isClosed(),
						baseValueLeft.isLast());

				newBaseValueRight.setBaseContainer(baseValueRight.getBaseContainer());
				newBaseValueRight.setMetaAttribute(metaAttribute);

				if (choice == MergeResultChoice.RIGHT) {
					baseEntity.put(baseEntity.isSet() ? null : metaAttribute.getName(), newBaseValueRight);
				} else {
					baseEntity.put(baseEntity.isSet() ? null : metaAttribute.getName(), baseValueLeft);
				}

				baseEntityManager.registerAsUpdated(newBaseValueRight);

				if (metaType.isComplex() && !metaType.isReference()) {
					//baseEntityManager.registerAsDeleted((IPersistable)baseValueRight.getValue());
//                    baseEntityManager.registerUnusedBaseEntity((IBaseEntity) baseValueRight.getValue());
					if (deleteUnused) {
						BaseEntity be = (BaseEntity) baseValueRight.getValue();

						if (!isEntityUsedElse(be.getId(), baseValueRight.getBaseContainer().getId())) {
							be.setOperation(OperationType.DELETE);
							registerAsDeleted(baseEntityManager, metaType, baseValueRight);
						}
					}
				}

			} else if (mergeManager.getAction() == IBaseEntityMergeManager.Action.KEEP_RIGHT) {
				IBaseValue newBaseValueLeft = BaseValueFactory.create(
						MetaContainerTypes.META_CLASS,
						metaType,
						baseValueLeft.getId(),
						creditorId,
						new Date(baseValueRight.getRepDate().getTime()),
						baseValueRight.getValue(),
						baseValueRight.isClosed(),
						baseValueRight.isLast());

				newBaseValueLeft.setBaseContainer(baseValueLeft.getBaseContainer());
				newBaseValueLeft.setMetaAttribute(metaAttribute);

				if (choice == MergeResultChoice.LEFT) {
					baseEntity.put(baseEntity.isSet() ? null : metaAttribute.getName(), newBaseValueLeft);
				} else {
					baseEntity.put(baseEntity.isSet() ? null : metaAttribute.getName(), baseValueRight);
				}

				baseEntityManager.registerAsUpdated(newBaseValueLeft);
				if (metaType.isComplex() && !metaType.isReference()) {
					//baseEntityManager.registerAsDeleted((IPersistable) baseValueLeft.getValue());
//                    baseEntityManager.registerUnusedBaseEntity((IBaseEntity) baseValueLeft.getValue());
					if (deleteUnused) {
						BaseEntity be = (BaseEntity) baseValueLeft.getValue();

						if (!isEntityUsedElse(be.getId(), baseValueLeft.getBaseContainer().getId())) {
							be.setOperation(OperationType.DELETE);
							registerAsDeleted(baseEntityManager, metaType, baseValueLeft);
						}
					}
				}

			}
			if (mergeManager.getAction() == IBaseEntityMergeManager.Action.TO_MERGE) {
				throw new RuntimeException(String.valueOf(Errors.E108));
			}
		}

		if (mergeManager.getChildMap() != null) {

			// we haven't reached the base case - do recursion
			IBaseEntity baseEntityLeft = (IBaseEntity) baseValueLeft.getValue();
			IBaseEntity baseEntityRight = (IBaseEntity) baseValueRight.getValue();


			MergeManagerKey<Long> idKey = new MergeManagerKey<Long>(new Long(baseEntityLeft.getId()), new Long(baseEntityRight.getId()));
			for (IBaseEntityMergeManager baseEntityMergeManager : mergeManager.getChildManager(idKey)) {
				IBaseEntityMergeManager childMergeManager = baseEntityMergeManager;
				IBaseEntity currentApplied = null;
				if (childMergeManager == null) {
					currentApplied = mergeBaseEntity(baseEntityLeft, baseEntityRight, mergeManager, baseEntityManager, choice, deleteUnused);
				} else {
					currentApplied = mergeBaseEntity(baseEntityLeft, baseEntityRight, childMergeManager, baseEntityManager, choice, deleteUnused);
				}

				if (mergeManager.getAction() == IBaseEntityMergeManager.Action.KEEP_BOTH) {
					if (choice == MergeResultChoice.LEFT) {
						IBaseValue newBaseValueLeft = BaseValueFactory.create(
								MetaContainerTypes.META_CLASS,
								metaType,
								baseValueLeft.getId(),
								creditorId,
								new Date(baseValueRight.getRepDate().getTime()),
								currentApplied,
								baseValueRight.isClosed(),
								baseValueRight.isLast());
						baseEntity.put(baseEntity.isSet() ? null : metaAttribute.getName(), newBaseValueLeft);

					} else {
						baseEntity.put(baseEntity.isSet() ? null : metaAttribute.getName(), baseValueRight);
						IBaseValue newBaseValueRight = BaseValueFactory.create(
								MetaContainerTypes.META_CLASS,
								metaType,
								baseValueRight.getId(),
								creditorId,
								new Date(baseValueLeft.getRepDate().getTime()),
								currentApplied,
								baseValueLeft.isClosed(),
								baseValueLeft.isLast());
						baseEntity.put(baseEntity.isSet() ? null : metaAttribute.getName(), newBaseValueRight);
					}
				}
			}
		}
	}

	private boolean isEntityUsedElse(long entityIdToCheck, long entityIdContaining) {
		IBaseEntityDao baseEntityDao = persistableDaoPool
				.getPersistableDao(BaseEntity.class, IBaseEntityDao.class);
		boolean used = baseEntityDao.isUsed(entityIdToCheck, entityIdContaining);
		return used;
	}
}
