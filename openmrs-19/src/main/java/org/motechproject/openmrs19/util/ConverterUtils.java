package org.motechproject.openmrs19.util;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.motechproject.openmrs19.domain.OpenMRSAttribute;
import org.motechproject.openmrs19.domain.OpenMRSConcept;
import org.motechproject.openmrs19.domain.OpenMRSConceptName;
import org.motechproject.openmrs19.domain.OpenMRSEncounterType;
import org.motechproject.openmrs19.domain.OpenMRSFacility;
import org.motechproject.openmrs19.domain.OpenMRSObservation;
import org.motechproject.openmrs19.domain.OpenMRSPatient;
import org.motechproject.openmrs19.domain.OpenMRSPerson;
import org.motechproject.openmrs19.domain.OpenMRSProvider;
import org.motechproject.openmrs19.domain.OpenMRSUser;
import org.motechproject.openmrs19.resource.model.Attribute;
import org.motechproject.openmrs19.resource.model.Concept;
import org.motechproject.openmrs19.resource.model.ConceptListResult;
import org.motechproject.openmrs19.resource.model.Encounter;
import org.motechproject.openmrs19.resource.model.Identifier;
import org.motechproject.openmrs19.resource.model.IdentifierType;
import org.motechproject.openmrs19.resource.model.Location;
import org.motechproject.openmrs19.resource.model.Observation;
import org.motechproject.openmrs19.resource.model.Patient;
import org.motechproject.openmrs19.resource.model.Person;
import org.motechproject.openmrs19.resource.model.Person.PreferredAddress;
import org.motechproject.openmrs19.resource.model.Person.PreferredName;
import org.motechproject.openmrs19.resource.model.Provider;
import org.motechproject.openmrs19.resource.model.User;

import java.util.ArrayList;
import java.util.List;

public final class ConverterUtils {

    private ConverterUtils() {
    }

    public static OpenMRSPerson toOpenMRSPerson(Person person) {
        OpenMRSPerson converted = new OpenMRSPerson();
        converted.setId(person.getUuid());
        PreferredName personName = person.getPreferredName();
        if (personName != null) {
            converted.setFirstName(personName.getGivenName());
            converted.setMiddleName(personName.getMiddleName());
            converted.setLastName(personName.getFamilyName());
        }

        converted.setGender(person.getGender());

        if (person.getPreferredAddress() != null) {
            converted.setAddress(person.getPreferredAddress().getAddress1());
        }

        if (person.getBirthdate() != null) {
            converted.setDateOfBirth(new DateTime(person.getBirthdate()));
        }

        if (person.getDeathDate() != null) {
            converted.setDeathDate(new DateTime(person.getDeathDate()));
        }

        converted.setIsDead(person.isDead());

        if (person.getAttributes() != null) {
            for (Attribute attr : person.getAttributes()) {
                // extract name/value from the display property
                // there is no explicit property for name attribute
                // the display attribute is formatted as: name = value
                String display = attr.getDisplay();
                int index = display.indexOf('=');
                String name = display.substring(0, index).trim();

                converted.addAttribute(new OpenMRSAttribute(name, attr.getValue()));
            }
        }

        return converted;
    }

    public static Person toPerson(OpenMRSPerson person, boolean includeNames) {
        Person converted = new Person();
        converted.setUuid(person.getPersonId());
        if (person.getDateOfBirth() != null) {
            converted.setBirthdate(person.getDateOfBirth().toDate());
        }
        if (person.getDeathDate() != null) {
            converted.setDeathDate(person.getDeathDate().toDate());
        }
        converted.setBirthdateEstimated((Boolean) ObjectUtils.defaultIfNull(person.getBirthDateEstimated(), false));
        converted.setDead(person.isDead());
        converted.setGender(person.getGender());

        if (includeNames) {
            PreferredName name = new PreferredName();
            name.setGivenName(person.getFirstName());
            name.setMiddleName(person.getMiddleName());
            name.setFamilyName(person.getLastName());
            List<PreferredName> names = new ArrayList<PreferredName>();
            names.add(name);
            converted.setNames(names);

            PreferredAddress address = new PreferredAddress();
            address.setAddress1(person.getAddress());
            List<PreferredAddress> addresses = new ArrayList<PreferredAddress>();
            addresses.add(address);
            converted.setAddresses(addresses);
        }

        return converted;
    }

    public static OpenMRSFacility toOpenMRSFacility(Location location) {
        return new OpenMRSFacility(location.getUuid(), location.getName(), location.getCountry(), location.getAddress6(),
                location.getCountyDistrict(), location.getStateProvince());
    }

    public static Location toLocation(OpenMRSFacility facility) {
        Location location = new Location();
        location.setAddress6(facility.getRegion());
        location.setDescription(facility.getName());
        location.setCountry(facility.getCountry());
        location.setCountyDistrict(facility.getCountyDistrict());
        location.setName(facility.getName());
        location.setStateProvince(facility.getStateProvince());
        location.setUuid(facility.getFacilityId());
        return location;
    }

    public static OpenMRSObservation toOpenMRSObservation(Observation ob) {
        OpenMRSObservation obs = new OpenMRSObservation(ob.getUuid(), ob.getObsDatetime(), ob.getConcept().getDisplay(), 
            ob.getValue().getDisplay());
        if (ob.getEncounter() != null && ob.getEncounter().getPatient() != null) {
            obs.setPatientId(ob.getEncounter().getPatient().getUuid());
        }
        return obs;
    }

    public static Observation toObservation(OpenMRSObservation openMRSObservation) {
        Observation observation = new Observation();
        observation.setUuid(openMRSObservation.getObservationId());
        observation.setObsDatetime(openMRSObservation.getDate().toDate());
        Observation.ObservationValue observationValue = new Observation.ObservationValue();
        observationValue.setDisplay(openMRSObservation.getValue().toString());
        observation.setValue(observationValue);

        Concept concept = new Concept();
        Concept.ConceptName conceptName = new Concept.ConceptName();
        conceptName.setName(openMRSObservation.getConceptName());
        concept.setName(conceptName);

        observation.setConcept(concept);

        return observation;
    }

    public static OpenMRSPatient createPatient(OpenMRSPatient patient) {
        OpenMRSFacility facility = patient.getFacility();
        OpenMRSFacility openMRSFacility = null;
        if (facility != null) {
            openMRSFacility = new OpenMRSFacility(facility.getFacilityId());
            openMRSFacility.setCountry(facility.getCountry());
            openMRSFacility.setCountyDistrict(facility.getCountyDistrict());
            openMRSFacility.setName(facility.getName());
            openMRSFacility.setRegion(facility.getRegion());
            openMRSFacility.setStateProvince(facility.getStateProvince());
        }

        OpenMRSPerson openMRSPerson = createPerson(patient.getPerson());

        OpenMRSPatient openMRSPatient = new OpenMRSPatient(patient.getMotechId());
        openMRSPatient.setPatientId(patient.getPatientId());
        openMRSPatient.setFacility(openMRSFacility);
        openMRSPatient.setPerson(openMRSPerson);
        openMRSPatient.setMotechId(patient.getMotechId());

        return openMRSPatient;
    }

    public static OpenMRSPerson createPerson(OpenMRSPerson personMrs) {
        List<OpenMRSAttribute> attributeList = createAttributeList(personMrs.getAttributes());

        OpenMRSPerson person = new OpenMRSPerson();
        person.setId(personMrs.getId());
        person.setAddress(personMrs.getAddress());
        person.setFirstName(personMrs.getFirstName());
        person.setLastName(personMrs.getLastName());
        person.setAge(personMrs.getAge());
        person.setBirthDateEstimated(personMrs.getBirthDateEstimated());
        person.setDateOfBirth(personMrs.getDateOfBirth());
        if (personMrs.isDead() != null) {
            person.setIsDead(personMrs.isDead());
        }
        person.setDeathDate(personMrs.getDeathDate());
        person.setGender(personMrs.getGender());
        person.setMiddleName(personMrs.getMiddleName());
        person.setPreferredName(personMrs.getPreferredName());
        person.setAttributes(attributeList);

        return person;
    }

    public static List<OpenMRSAttribute> createAttributeList(List<OpenMRSAttribute> attributesMrs) {
        List<OpenMRSAttribute> attributeList = new ArrayList<>();

        if (attributesMrs != null) {
            for (OpenMRSAttribute attribute : attributesMrs) {
                attributeList.add(new OpenMRSAttribute(attribute.getName(), attribute.getValue()));
            }
        }
        return  attributeList;
    }

    public static Concept toConcept(OpenMRSConcept openMRSConcept) {
        Concept concept = new Concept();
        concept.setUuid(openMRSConcept.getUuid());
        concept.setDisplay(openMRSConcept.getDisplay());
        concept.setName(openMRSConcept.getName() != null ? createConceptName(openMRSConcept.getName()) : null);

        if (openMRSConcept.getNames() != null) {
            List<Concept.ConceptName> names = new ArrayList<>();
            for (OpenMRSConceptName conceptName : openMRSConcept.getNames()) {
                names.add(createConceptName(conceptName));
            }
            concept.setNames(names);
        }

        if (openMRSConcept.getConceptClass() != null) {
            Concept.ConceptClass conceptClass = new Concept.ConceptClass();
            conceptClass.setDisplay(openMRSConcept.getConceptClass());
            concept.setConceptClass(conceptClass);
        }

        if (openMRSConcept.getDataType() != null) {
            Concept.DataType dataType = new Concept.DataType();
            dataType.setDisplay(openMRSConcept.getDataType());
            concept.setDatatype(dataType);
        }

        return concept;
    }

    public static OpenMRSConcept toOpenMRSConcept(Concept concept) {
        OpenMRSConcept openMRSConcept = new OpenMRSConcept();

        openMRSConcept.setName(concept.getName() == null ? null : new OpenMRSConceptName(concept.getName()));
        openMRSConcept.setUuid(concept.getUuid());
        openMRSConcept.setDataType(concept.getDatatype() == null ? null : concept.getDatatype().getDisplay());
        openMRSConcept.setConceptClass(concept.getConceptClass() == null ? null : concept.getConceptClass().getDisplay());
        openMRSConcept.setDisplay(concept.getDisplay());

        if (concept.getNames() != null) {
            List<OpenMRSConceptName> names = new ArrayList<>();

            for (Concept.ConceptName conceptName : concept.getNames()) {
                names.add(new OpenMRSConceptName(conceptName));
            }

            openMRSConcept.setNames(names);
        }

        return openMRSConcept;
    }

    public static OpenMRSUser toOpenMRSUser(User user) {

        OpenMRSUser openMRSUser = new OpenMRSUser();

        openMRSUser.setUserName(user.getUsername());
        openMRSUser.setUserId(user.getUuid());
        openMRSUser.setSecurityRole(user.getFirstRole());
        openMRSUser.setSystemId(user.getSystemId());
        openMRSUser.setPerson(user.getPerson() != null ? toOpenMRSPerson(user.getPerson()) : null);

        return openMRSUser;
    }

    private static Concept.ConceptName createConceptName(OpenMRSConceptName openMRSConceptName) {
        Concept.ConceptName conceptName = new Concept.ConceptName();
        conceptName.setName(openMRSConceptName.getName());
        conceptName.setLocale(openMRSConceptName.getLocale());
        conceptName.setConceptNameType(openMRSConceptName.getConceptNameType());

        return conceptName;
    }

    public static OpenMRSPatient toOpenMRSPatient(Patient patient) {
        return toOpenMRSPatient(patient, null, null);
    }

    public static OpenMRSPatient toOpenMRSPatient(Patient patient, OpenMRSFacility facility, String motechId) {

        OpenMRSPatient openMRSPatient = new OpenMRSPatient(patient.getUuid());

        openMRSPatient.setPerson(toOpenMRSPerson(patient.getPerson()));
        openMRSPatient.setFacility(facility);
        openMRSPatient.setMotechId(motechId);

        return openMRSPatient;
    }

    public static Patient toPatient(OpenMRSPatient patient, OpenMRSPerson savedPerson, String motechPatientIdentifierTypeUuid) {

        Patient converted = new Patient();
        Person person = new Person();
        person.setUuid(savedPerson.getPersonId());
        converted.setPerson(person);

        Location location = null;
        if (patient.getFacility() != null && StringUtils.isNotBlank(patient.getFacility().getFacilityId())) {
            location = new Location();
            location.setUuid(patient.getFacility().getFacilityId());
        }

        IdentifierType type = new IdentifierType();
        type.setUuid(motechPatientIdentifierTypeUuid);

        Identifier identifier = new Identifier();
        identifier.setIdentifier(patient.getMotechId());
        identifier.setLocation(location);
        identifier.setIdentifierType(type);

        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);
        converted.setIdentifiers(identifiers);

        return converted;
    }

    public static OpenMRSProvider toOpenMRSProvider(Provider provider) {
        OpenMRSProvider openMRSProvider = new OpenMRSProvider();

        openMRSProvider.setProviderId(provider.getUuid());
        openMRSProvider.setPerson(provider.getPerson() != null ? toOpenMRSPerson(provider.getPerson()) : null);
        openMRSProvider.setIdentifier(provider.getIdentifier());

        return openMRSProvider;
    }

    public static Provider toProvider(OpenMRSProvider openMRSProvider) {

        Provider provider = new Provider();

        provider.setUuid(openMRSProvider.getProviderId());
        provider.setPerson(openMRSProvider.getPerson() != null ? toPerson(openMRSProvider.getPerson(), false) : null);
        provider.setIdentifier(openMRSProvider.getIdentifier());

        return provider;
    }

    public static OpenMRSEncounterType toOpenMRSEncounterType(Encounter.EncounterType encounterType) {

        OpenMRSEncounterType openMRSEncounterType = new OpenMRSEncounterType();

        openMRSEncounterType.setUuid(encounterType.getUuid());
        openMRSEncounterType.setName(encounterType.getName());
        openMRSEncounterType.setDescription(encounterType.getDescription());

        return openMRSEncounterType;
    }

    public static Encounter.EncounterType toEncounterType(OpenMRSEncounterType openMRSEncounterType) {

        Encounter.EncounterType encounterType = new Encounter.EncounterType();

        encounterType.setUuid(openMRSEncounterType.getUuid());
        encounterType.setName(openMRSEncounterType.getName());
        encounterType.setDescription(openMRSEncounterType.getDescription());

        return encounterType;
    }

    public static List<OpenMRSConcept> toOpenMRSConcepts(ConceptListResult conceptListResult) {

        List<OpenMRSConcept> concepts = new ArrayList<>();

        for (Concept concept : conceptListResult.getResults()) {
            concepts.add(toOpenMRSConcept(concept));
        }

        return concepts;
    }
}
