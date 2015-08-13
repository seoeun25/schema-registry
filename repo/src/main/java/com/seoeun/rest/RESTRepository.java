package com.seoeun.rest;

import com.seoeun.AvroRepoException;
import com.seoeun.jpa.QueryExecutor;
import com.seoeun.schemaregistry.SchemaInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/")
public class RESTRepository {

    private static Logger LOG = LoggerFactory.getLogger(RESTRepository.class);

    private QueryExecutor queryExecutor;

    public RESTRepository() {
        // TODO : inject
        queryExecutor = new QueryExecutor();
    }

    @GET
    @Path("schema/ids/{id}")
    public Response getSchemabyId(@PathParam("id") String id) {
        try {
            long idValue = Long.parseLong(id);
            SchemaInfo schemaInfo = queryExecutor.get(QueryExecutor.SchemaInfoQuery.GET_BYID, new
                    Object[]{idValue});
            return Response.status(200).entity(schemaInfo.toJsonOblect().toJSONString()).build();
        } catch (AvroRepoException e) {
            return Response.status(404).entity("Schema Not Found").build();
        } catch (Exception e) {
            return Response.status(500).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("subjects")
    @Produces({"text/plain"})
    public Response getSubjectList() {
        try {
            List<SchemaInfo> list = queryExecutor.getList(QueryExecutor.SchemaInfoQuery.GET_ALL);
            StringBuilder stringBuilder = new StringBuilder();
            for (SchemaInfo schemaInfo : list) {
                stringBuilder.append(schemaInfo.toJsonOblect().toJSONString() + "\n");
            }
            return Response.status(200).entity(stringBuilder.toString()).build();
        } catch (Exception e) {
            return Response.status(500).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("subjects/{subject}")
    @Produces("application/json")
    public Response getSchema(@PathParam("subject") String topicName) {
        try {
            SchemaInfo schemaInfo = queryExecutor.getListLimit1(QueryExecutor.SchemaInfoQuery.GET_BYTOPICLATEST, new
                    Object[]{topicName});
            return Response.status(200).entity(schemaInfo.toJsonOblect().toJSONString()).build();
        } catch (AvroRepoException e) {
            return Response.status(404).entity("Schema Not Found").build();
        } catch (Exception e) {
            return Response.status(500).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("subjects/{subject}/ids/{id}")
    @Produces("application/json")
    public Response getSchema(@PathParam("subject") String topicName, @PathParam("id") String id) {
        try {
            long idValue = Long.parseLong(id);
            SchemaInfo schemaInfo = queryExecutor.get(QueryExecutor.SchemaInfoQuery.GET_BYTOPICANDID, new Object[]{topicName,
                    idValue});
            return Response.status(200).entity(schemaInfo.toJsonOblect().toJSONString()).build();
        } catch (AvroRepoException e) {
            return Response.status(404).entity("Schema Not Found").build();
        } catch (Exception e) {
            return Response.status(500).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("subjects/{subject}")
    public Response registerSchema(@PathParam("subject") String subject, @FormParam("schema") String schema) {
        LOG.info(" register : " + subject + "\t " + schema);
        SchemaInfo schemaInfo = null;
        try {
            schemaInfo = queryExecutor.getListLimit1(QueryExecutor.SchemaInfoQuery.GET_BYTOPICLATEST, new Object[]{subject});
        } catch (AvroRepoException e) {
            // not exist, need to insert
        } catch (Exception e) {
            return Response.status(500).entity(e.getMessage()).build();
        }

        try {
            if (schemaInfo == null || !schemaInfo.getSchemaStr().equals(schema)) {
                schemaInfo = new SchemaInfo(subject, schema);
                Long obj = (Long)queryExecutor.insert(schemaInfo);
                LOG.info("-- new registered id :" + obj + " / " + subject);
                schemaInfo.setId(obj.longValue());
            } else {
                LOG.info("already exist : " + schemaInfo.getId() + " / " + subject);
            }
            return Response.status(200).entity(String.valueOf(schemaInfo.getId())).build();
        } catch (Exception e) {
            return Response.status(500).entity(e.getMessage()).build();
        }

    }
}
