cp --parent -R usci/modules/eav/modules/persistance/src/main/resources/properties/main.properties /c/bac_bauka
cp --parent -R usci/modules/eav/modules/persistance/src/main/resources/properties/oracle.properties /c/bac_bauka
cp --parent -R usci/modules/receiver/src/main/resources/properties/oracle.properties /c/bac_bauka
cp --parent -R usci/modules/showcase/src/main/resources/properties/oracle.properties /c/bac_bauka


 event = (XMLEvent) xmlReader.next();
            String crCode = event.asCharacters().getData();
            IBaseEntityProcessorDao processorDao = baseEntityRepository.getBaseEntityProcessorDao();
            RefListResponse refListResponse = processorDao.getRefListResponse(
                    metaClassRepository.getMetaClass("ref_creditor").getId(), batch.getRepDate(), false);

            boolean found = false;

            for(Map<String,Object> m : refListResponse.getData())
                if(m.get("CODE") != null && m.get("CODE").equals(crCode)){
                    long creditorId = ((BigDecimal)m.get("ID")).longValue();
                    IBaseEntity loadedCreditor = processorDao.load(creditorId);
                    BaseSet creditorDocs = (BaseSet)loadedCreditor.getEl("docs");
                    currentBaseEntity.put("docs",new BaseEntityComplexSet(batch,index,creditorDocs));
                    found = true;
                    break;
                }

            if(!found)
                currentBaseEntity.addValidationError(String.format("???????? ? ????? %s ?? ??????", crCode));