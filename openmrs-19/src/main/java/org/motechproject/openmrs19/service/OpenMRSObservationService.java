package org.motechproject.openmrs19.service;

import org.motechproject.openmrs19.domain.OpenMRSObservation;
import org.motechproject.openmrs19.exception.ObservationNotFoundException;

import java.util.List;

/**
 * Interface for fetching and voiding observation details.
 */
public interface OpenMRSObservationService {
    /**
     * Voids an observation for the MOTECH user, with the given reason.
     *
     * @param mrsObservation  OpenMRSObservation to be voided
     * @param reason  reason for voiding the OpenMRSObservation
     * @throws ObservationNotFoundException  if the expected Observation does not exist
     */
    void voidObservation(OpenMRSObservation mrsObservation, String reason) throws ObservationNotFoundException;

    /**
     * Returns the latest OpenMRSObservation of the MRS patient, given the concept name (e.g. WEIGHT).
     *
     * @param patientMotechId  MOTECH ID of the patient
     * @param conceptName  concept Name of the OpenMRSObservation
     * @return OpenMRSObservation if present
     */
    OpenMRSObservation findObservation(String patientMotechId, String conceptName);

    /**
     * Returns all matching OpenMRSObservations of the MRS patient, given the concept name (e.g. WEIGHT).
     *
     * @param patientMotechId  MOTECH ID of the patient
     * @param conceptName  concept Name of the OpenMRSObservation
     * @return list of OpenMRSObservation objects if present
     */
    List<OpenMRSObservation> findObservations(String patientMotechId, String conceptName);

    /**
     * Fetches an observation by the given observation ID.
     *
     * @param id  ID of the concept
     * @return OpenMRSObservation if present
     */
    OpenMRSObservation getObservationById(String id);

    /**
     * Saves the given observation on the OpenMRS server.
     *
     * @param observation  the observation to be created
     * @return  the created observation
     */
    OpenMRSObservation createObservation(OpenMRSObservation observation);

    /**
     * Deletes observation with the given ID.
     */
    void deleteObservation(String uuid);
}
