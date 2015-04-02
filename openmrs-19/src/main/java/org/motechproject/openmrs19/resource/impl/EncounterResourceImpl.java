package org.motechproject.openmrs19.resource.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.motechproject.openmrs19.OpenMrsInstance;
import org.motechproject.openmrs19.resource.model.Provider;
import org.motechproject.openmrs19.resource.model.Provider.ProviderSerializer;
import org.motechproject.openmrs19.exception.HttpException;
import org.motechproject.openmrs19.rest.RestClient;
import org.motechproject.openmrs19.resource.EncounterResource;
import org.motechproject.openmrs19.resource.model.Concept;
import org.motechproject.openmrs19.resource.model.Concept.ConceptSerializer;
import org.motechproject.openmrs19.resource.model.Encounter;
import org.motechproject.openmrs19.resource.model.Encounter.EncounterType;
import org.motechproject.openmrs19.resource.model.Encounter.EncounterTypeSerializer;
import org.motechproject.openmrs19.resource.model.EncounterListResult;
import org.motechproject.openmrs19.resource.model.Location;
import org.motechproject.openmrs19.resource.model.Location.LocationSerializer;
import org.motechproject.openmrs19.resource.model.Observation.ObservationValue;
import org.motechproject.openmrs19.resource.model.Observation.ObservationValueDeserializer;
import org.motechproject.openmrs19.resource.model.Observation.ObservationValueSerializer;
import org.motechproject.openmrs19.resource.model.Patient;
import org.motechproject.openmrs19.resource.model.Patient.PatientSerializer;
import org.motechproject.openmrs19.resource.model.Person;
import org.motechproject.openmrs19.resource.model.Person.PersonSerializer;
import org.motechproject.openmrs19.util.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

@Component
public class EncounterResourceImpl implements EncounterResource {

    private final RestClient restClient;
    private final OpenMrsInstance openmrsInstance;

    @Autowired
    public EncounterResourceImpl(RestClient restClient, OpenMrsInstance openmrsInstance) {
        this.restClient = restClient;
        this.openmrsInstance = openmrsInstance;
    }

    @Override
    public Encounter createEncounter(Encounter encounter) throws HttpException {
        Gson gson = new GsonBuilder().registerTypeAdapter(Location.class, new LocationSerializer())
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                .registerTypeAdapter(Patient.class, new PatientSerializer())
                .registerTypeAdapter(Person.class, new PersonSerializer())
                .registerTypeAdapter(Provider.class, new ProviderSerializer())
                .registerTypeAdapter(Concept.class, new ConceptSerializer())
                .registerTypeAdapter(EncounterType.class, new EncounterTypeSerializer())
                .registerTypeAdapter(ObservationValue.class, new ObservationValueSerializer()).create();

        String requestJson = gson.toJson(encounter);

        String responseJson = restClient.postForJson(openmrsInstance.toInstancePath("/encounter?v=full"), requestJson);
        return (Encounter) JsonUtils.readJson(responseJson, Encounter.class);
    }

    @Override
    public EncounterListResult queryForAllEncountersByPatientId(String id) throws HttpException {
        String responseJson = restClient.getJson(openmrsInstance.toInstancePathWithParams(
                "/encounter?patient={id}&v=full", id));

        Map<Type, Object> adapters = new HashMap<Type, Object>();
        adapters.put(ObservationValue.class, new ObservationValueDeserializer());
        EncounterListResult result = (EncounterListResult) JsonUtils.readJsonWithAdapters(responseJson,
                EncounterListResult.class, adapters);

        return result;
    }

    @Override
    public Encounter getEncounterById(String uuid) throws HttpException {
        String responseJson = restClient.getJson(openmrsInstance.toInstancePathWithParams("/encounter/{uuid}?v=full",
                uuid));
        Map<Type, Object> adapters = new HashMap<Type, Object>();
        adapters.put(ObservationValue.class, new ObservationValueDeserializer());
        return (Encounter) JsonUtils.readJsonWithAdapters(responseJson, Encounter.class, adapters);
    }

    @Override
    public EncounterType createEncounterType(EncounterType encounterType) throws HttpException {
        Gson gson = new GsonBuilder().create();
        String requestJson = gson.toJson(encounterType, EncounterType.class);
        String responseJson = restClient.postForJson(openmrsInstance.toInstancePath("/encountertype"), requestJson);
        return (EncounterType) JsonUtils.readJson(responseJson, EncounterType.class);
    }

    @Override
    public EncounterType getEncounterTypeByUuid(String uuid) throws HttpException {
        String responseJson = restClient.getJson(openmrsInstance.toInstancePathWithParams("/encountertype/{uuid}", uuid));
        return (EncounterType) JsonUtils.readJson(responseJson, EncounterType.class);
    }

    @Override
    public void deleteEncounterType(String uuid) throws HttpException {
        restClient.delete(openmrsInstance.toInstancePathWithParams("/encountertype/{uuid}?purge", uuid));
    }

    @Override
    public void deleteEncounter(String uuid) throws HttpException {
        restClient.delete(openmrsInstance.toInstancePathWithParams("/encounter/{uuid}?purge", uuid));
    }
}
