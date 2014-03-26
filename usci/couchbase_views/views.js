//http://blog.couchbase.com/understanding-grouplevel-view-queries-compound-keys
//batch

function (doc, meta) {
   if(doc.type == "batch") {
     emit(meta.id.substring(6), {id: doc.id, fileName: doc.fileName, received: doc.received});
   }
   if(doc.type == "batch_status") {
     emit(meta.id.substring(13), {batchStatuses: doc.batchStatuses});
   }
 }

function(key, values, rereduce) {
          var obj = values[1];
          obj["status"] = values[0];

          return values[1];
        }

//batch_pending

function (doc, meta) {
  if(doc.type == "batch_status" && doc.batchStatuses[doc.batchStatuses.length - 1].protocol != "COMPLETED") {
    emit(meta.id.substring(13), doc.batchStatuses[doc.batchStatuses.length - 1].protocol);
  }
}

//batch_sign

function (doc, meta) {
              if(doc.type == "sign" && !(doc.hasOwnProperty("sign"))) {
                emit(doc.userId, doc);
              }
            }

//contract_status
//use key filter - [batch_id, index]

function (doc, meta) {
  if(doc.type == "contract_status") {
    emit([doc.batchId, doc.index], {index: doc.index, contractStatuses: doc.contractStatuses});
  }
}

