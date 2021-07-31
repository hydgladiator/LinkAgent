package com.pamirs.attach.plugin.mongodb.common;

import com.mongodb.MongoNamespace;
import com.mongodb.internal.operation.*;
import com.pamirs.pradar.Pradar;
import com.pamirs.pradar.exception.PressureMeasureError;

import java.util.concurrent.TimeUnit;

/**
 * @author angju
 * @date 2020/8/15 10:28
 */
public class MongoOperationUtil {

    public static boolean retryWrites = false;

    /**
     * 影子表
     *
     * @param operation
     * @return
     */
    public static WriteOperation generateWriteOperationWithShadowTable(final WriteOperation operation) {

        if (operation instanceof InsertOperation) {
            if (Pradar.isClusterTestPrefix(((InsertOperation) operation).getNamespace().getDatabaseName()) ||
                    Pradar.isClusterTestPrefix(((InsertOperation) operation).getNamespace().getCollectionName())) {
                return operation;
            }
            MongoNamespace ptNamespace;
            ptNamespace = new MongoNamespace(((InsertOperation) operation).getNamespace().getDatabaseName(), Pradar.addClusterTestPrefix(((InsertOperation) operation).getNamespace().getCollectionName()));
            return new InsertOperation(ptNamespace, ((InsertOperation) operation).isOrdered(), ((InsertOperation) operation).getWriteConcern(),
                    retryWrites, ((InsertOperation) operation).getInsertRequests())
                    .bypassDocumentValidation(((InsertOperation) operation).getBypassDocumentValidation());
        } else if (operation instanceof UpdateOperation) {
            if (Pradar.isClusterTestPrefix(((UpdateOperation) operation).getNamespace().getDatabaseName()) ||
                    Pradar.isClusterTestPrefix(((UpdateOperation) operation).getNamespace().getCollectionName())) {
                return operation;
            }
            MongoNamespace ptNamespace;
            ptNamespace = new MongoNamespace(((UpdateOperation) operation).getNamespace().getDatabaseName(), Pradar.addClusterTestPrefix(((UpdateOperation) operation).getNamespace().getCollectionName()));
            return new UpdateOperation(ptNamespace, ((UpdateOperation) operation).isOrdered(), ((UpdateOperation) operation).getWriteConcern(),
                    retryWrites, ((UpdateOperation) operation).getUpdateRequests())
                    .bypassDocumentValidation(((UpdateOperation) operation).getBypassDocumentValidation());
        } else if (operation instanceof DeleteOperation) {
            if (Pradar.isClusterTestPrefix(((DeleteOperation) operation).getNamespace().getDatabaseName()) ||
                    Pradar.isClusterTestPrefix(((DeleteOperation) operation).getNamespace().getCollectionName())) {
                return operation;
            }
            MongoNamespace ptNamespace;
            ptNamespace = new MongoNamespace(((DeleteOperation) operation).getNamespace().getDatabaseName(), Pradar.addClusterTestPrefix(((DeleteOperation) operation).getNamespace().getCollectionName()));
            return new DeleteOperation(ptNamespace, ((DeleteOperation) operation).isOrdered(), ((DeleteOperation) operation).getWriteConcern(),
                    retryWrites, ((DeleteOperation) operation).getDeleteRequests())
                    .bypassDocumentValidation(((DeleteOperation) operation).getBypassDocumentValidation());
        } else if (operation instanceof MixedBulkWriteOperation) {
            if (Pradar.isClusterTestPrefix(((MixedBulkWriteOperation) operation).getNamespace().getDatabaseName()) ||
                    Pradar.isClusterTestPrefix(((MixedBulkWriteOperation) operation).getNamespace().getCollectionName())) {
                return operation;
            }
            MongoNamespace ptNamespace;
            ptNamespace = new MongoNamespace(((MixedBulkWriteOperation) operation).getNamespace().getDatabaseName(), Pradar.addClusterTestPrefix(((MixedBulkWriteOperation) operation).getNamespace().getCollectionName()));
            return new MixedBulkWriteOperation(ptNamespace,
                    ((MixedBulkWriteOperation) operation).getWriteRequests(), ((MixedBulkWriteOperation) operation).isOrdered(),
                    ((MixedBulkWriteOperation) operation).getWriteConcern(), retryWrites)
                    .bypassDocumentValidation(((MixedBulkWriteOperation) operation).getBypassDocumentValidation());

        } else {
            throw new PressureMeasureError("压测下 mongodb Operation is not support ");
        }
    }


    /**
     * 根据原来的findOperation生成一个ptFindOpertion
     *
     * @param targetReadOperation
     * @return
     */
    public static FindOperation generateFromFindOperation(FindOperation targetReadOperation, MongoNamespace ptNamespace) {
        FindOperation ptOperation = new FindOperation(ptNamespace, targetReadOperation.getDecoder());
        ptOperation.filter(targetReadOperation.getFilter());
        ptOperation.batchSize(targetReadOperation.getBatchSize());
        ptOperation.skip(targetReadOperation.getSkip());
        ptOperation.limit(targetReadOperation.getLimit());
        ptOperation.maxAwaitTime(targetReadOperation.getMaxAwaitTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
        ptOperation.maxTime(targetReadOperation.getMaxTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
/*
        ptOperation.modifiers(targetReadOperation.getModifiers());
*/
        ptOperation.projection(targetReadOperation.getProjection());
        ptOperation.sort(targetReadOperation.getSort());
        ptOperation.collation(targetReadOperation.getCollation());
        ptOperation.cursorType(targetReadOperation.getCursorType());
        ptOperation.oplogReplay(targetReadOperation.isOplogReplay());
        ptOperation.noCursorTimeout(targetReadOperation.isNoCursorTimeout());
        ptOperation.partial(targetReadOperation.isPartial());
        return ptOperation;
    }


}
