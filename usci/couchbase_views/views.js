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
  if(doc.type == "batch_status" && doc.batchStatuses[doc.batchStatuses.length - 1].protocol != "PARSING_COMPLETED") {
    emit(meta.id.substring(13), doc.batchStatuses[doc.batchStatuses.length - 1].protocol);
  }
}

//batch_sign

function (doc, meta) {
              if(doc.type == "sign" && !(doc.hasOwnProperty("sign"))) {
                emit(doc.userId, doc);
              }
            }

//batch_statuses

function (doc, meta) {
          if(doc.type == "batch") {
            emit(meta.id.substring(6), [doc.id, doc.fileName]);
          }
          if(doc.type == "batch_status") {
            emit(meta.id.substring(13), [doc.batchStatuses[doc.batchStatuses.length - 1].protocol]);
          }
        }

function(key, values, rereduce) {
          var obj = values[values.length - 1];

          for (i = values.length - 2; i >= 0; i--) {
            obj = obj.concat(values[i]);
          }

          return obj;
        }

//contract_status

function (doc, meta) {
          if(doc.type == "contract_status") {
            var str = ("000000000000" + doc.index);
            emit(doc.batchId + "_" + (str.substring(str.length-10)), {index: doc.index, contractStatuses: doc.contractStatuses});
          }
        }

