package com.seoeun.client;

import com.seoeun.schemaregistry.SchemaInfo;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RepoClient {

    private String url;

    private Client client;

    public RepoClient(String url) {
        this.url = url;
        this.client = ClientBuilder.newClient();
    }

    /**
     * Register the schema under the subject.
     *
     * @param topicName the name of subject
     * @param schema    the avro schema
     * @return the id of the registered schema. <code>null</code> if failed.
     */
    public String register(String topicName, String schema) {
        Form form = new Form();
        form.param("subject", topicName);
        form.param("schema", schema);

        Response response = client.target(url).path("subjects/{subject}").resolveTemplate("subject", topicName)
                .request(MediaType.APPLICATION_JSON_TYPE).post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            String id = response.readEntity(String.class);
            return id;
        } else {
            response.close();
            return null;
        }
    }

    /**
     * Get all the schema.
     *
     * @return list<SchemaInfo>
     */
    public List<SchemaInfo> getSchemaLatestAll() {
        Response response = client.target(url).path("subjects/").request(MediaType.TEXT_PLAIN_TYPE).get();

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            List<SchemaInfo> schemaInfos = new ArrayList<SchemaInfo>();
            String[] subjects = response.readEntity(String.class).split("\n");
            for (String subject : subjects) {
                JSONObject jsonObject = (JSONObject) JSONValue.parse(subject);
                SchemaInfo schemaInfo = new SchemaInfo();
                schemaInfo.setId(Long.parseLong(jsonObject.get("id").toString()));
                schemaInfo.setName(jsonObject.get("name").toString());
                schemaInfo.setSchemaStr(jsonObject.get("schemaStr").toString());
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(Long.parseLong(jsonObject.get("created").toString()));
                schemaInfo.setCreated(calendar);
                schemaInfos.add(schemaInfo);
            }
            return schemaInfos;
        } else {
            response.close();
            return null;
        }
    }

    /**
     * Gets the schema by subject.
     *
     * @param subject the subject name
     * @return schemaInfo
     */
    public SchemaInfo getSchemaBySubject(String subject) {
        WebTarget target = client.target(url).path("subjects/{subject}").resolveTemplate("subject", subject);
        URI uri = target.getUri();
        Response response = client.target(url).path("subjects/{subject}").resolveTemplate("subject", subject)
                .request("application/json").get();
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            SchemaInfo schemaInfo = response.readEntity(SchemaInfo.class);
            return schemaInfo;
        } else {
            response.close();
            return null;
        }
    }

    /**
     * Gets the schema by subject and id
     *
     * @param subject the subject name
     * @param id      the subject id
     * @return schemaInfo
     */
    public SchemaInfo getSchemaBySubjectAndId(String subject, String id) {
        Response response = client.target(url).path("subjects/{subject}/ids/{id}").resolveTemplate("subject", subject)
                .resolveTemplate("id", id)
                .request("application/json").get();

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            SchemaInfo schemaInfo = response.readEntity(SchemaInfo.class);
            return schemaInfo;
        } else {
            response.close();
            return null;
        }
    }

    /**
     * Gets ths schema by id
     *
     * @param id
     * @return schemaInfo
     */
    public SchemaInfo getSchemaById(String id) {
        Response response = client.target(url).path("schema/ids/{id}").resolveTemplate("id", id)
                .request("application/json").get();
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            SchemaInfo schemaInfo = response.readEntity(SchemaInfo.class);
            return schemaInfo;
        } else {
            response.close();
            return null;
        }
    }

}
