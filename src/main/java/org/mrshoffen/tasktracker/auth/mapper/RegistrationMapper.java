package org.mrshoffen.tasktracker.auth.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mrshoffen.tasktracker.commons.kafka.event.registration.RegistrationAttemptEvent;
import org.mrshoffen.tasktracker.commons.kafka.event.registration.RegistrationSuccessfulEvent;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RegistrationMapper {

    RegistrationSuccessfulEvent buildSuccessfulRegistrationEvent(RegistrationAttemptEvent event);

}
