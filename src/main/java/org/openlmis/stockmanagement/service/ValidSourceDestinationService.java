/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org. 
 */

package org.openlmis.stockmanagement.service;

import org.openlmis.stockmanagement.domain.movement.Node;
import org.openlmis.stockmanagement.domain.movement.ValidDestinationAssignment;
import org.openlmis.stockmanagement.domain.movement.ValidSourceAssignment;
import org.openlmis.stockmanagement.dto.ValidSourceDestinationDto;
import org.openlmis.stockmanagement.repository.OrganizationRepository;
import org.openlmis.stockmanagement.repository.ValidDestinationAssignmentRepository;
import org.openlmis.stockmanagement.repository.ValidSourceAssignmentRepository;
import org.openlmis.stockmanagement.service.referencedata.FacilityReferenceDataService;
import org.openlmis.stockmanagement.service.referencedata.ProgramFacilityTypeExistenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ValidSourceDestinationService {

  @Autowired
  private ProgramFacilityTypeExistenceService programFacilityTypeExistenceService;

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private ValidDestinationAssignmentRepository validDestinationRepository;

  @Autowired
  private ValidSourceAssignmentRepository validSourceRepository;

  @Autowired
  private FacilityReferenceDataService facilityRefDataService;

  @Autowired
  private OrganizationRepository organizationRepository;

  /**
   * Find valid destinations by program ID and facility type ID.
   *
   * @param programId      program ID
   * @param facilityTypeId facility type ID
   * @return valid destination assignment DTOs
   */
  public List<ValidSourceDestinationDto> findSources(
      UUID programId, UUID facilityTypeId) {
    programFacilityTypeExistenceService.checkProgramAndFacilityTypeExist(programId, facilityTypeId);
    permissionService.canViewStockSource(programId, facilityTypeId);

    List<ValidSourceAssignment> sourceAssignments =
        validSourceRepository.findByProgramIdAndFacilityTypeId(programId, facilityTypeId);
    return sourceAssignments.stream()
        .map(source -> createDtoFrom(source.getNode()))
        .collect(Collectors.toList());
  }

  /**
   * Find valid sources by program ID and facility type ID.
   *
   * @param programId      program ID
   * @param facilityTypeId facility type ID
   * @return valid source assignment DTOs
   */
  public List<ValidSourceDestinationDto> findDestinations(UUID programId, UUID facilityTypeId) {
    programFacilityTypeExistenceService
        .checkProgramAndFacilityTypeExist(programId, facilityTypeId);
    permissionService
        .canViewStockDestinations(programId, facilityTypeId);

    List<ValidDestinationAssignment> destinationAssignments =
        validDestinationRepository.findByProgramIdAndFacilityTypeId(programId, facilityTypeId);
    return destinationAssignments.stream()
        .map(destination -> createDtoFrom(destination.getNode()))
        .collect(Collectors.toList());
  }

  private ValidSourceDestinationDto createDtoFrom(Node node) {
    ValidSourceDestinationDto dto = new ValidSourceDestinationDto();

    dto.setId(node.getId());
    boolean isRefDataFacility = node.isRefDataFacility();
    if (isRefDataFacility) {
      dto.setName(facilityRefDataService.findOne(node.getReferenceId()).getName());
    } else {
      dto.setName(organizationRepository.findOne(node.getReferenceId()).getName());
    }
    dto.setIsFreeTextAllowed(!isRefDataFacility);
    return dto;
  }

}