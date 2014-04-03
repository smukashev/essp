{
   "views":{
      "batch":{
         "map":"function (doc, meta) {
   if(doc.type == \"batch\") {
     emit(meta.id.substring(6), {id: doc.id, fileName: doc.fileName, received: doc.received});
   }
   if(doc.type == \"batch_status\") {
     emit(meta.id.substring(13), {batchStatuses: doc.batchStatuses});
   }
 }"
      },
      "batch_creditor":{
         "map":"function (doc, meta) {
   if(doc.type == \"batch\") {
     emit(doc.id, doc.creditorId);
   }
 }"
      },
      "batch_pending":{
         "map":"function (doc, meta) {
  if(doc.type == \"batch_status\" && doc.batchStatuses[doc.batchStatuses.length - 1].protocol != \"COMPLETED\") {
    emit(parseInt(meta.id.substring(13)), doc.batchStatuses[doc.batchStatuses.length - 1].protocol);
  }
}"
      },
      "batch_sign":{
         "map":"function (doc, meta) {
              if(doc.type == \"sign\" && !(doc.hasOwnProperty(\"sign\"))) {
                emit(doc.userId, doc);
              }
            }"
      },
      "contract_status":{
         "map":"function (doc, meta) {
  if(doc.type == \"contract_status\") {
    emit([doc.batchId, doc.index], {index: doc.index, contractStatuses: doc.contractStatuses});
  }
}"
      }
   }
}