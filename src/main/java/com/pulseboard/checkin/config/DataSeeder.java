package com.pulseboard.checkin.config;

import com.pulseboard.checkin.model.Attendee;
import com.pulseboard.checkin.repository.AttendeeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {

    @org.springframework.context.annotation.Bean
    CommandLineRunner seed(AttendeeRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }

            repository.save(Attendee.builder()
                    .fullName("Priya Nair")
                    .email("priya.nair@example.com")
                    .vip(true)
                    .checkedIn(false)
                    .build());

            repository.save(Attendee.builder()
                    .fullName("Marcus Chen")
                    .email("marcus.chen@example.com")
                    .vip(false)
                    .checkedIn(false)
                    .build());

            repository.save(Attendee.builder()
                    .fullName("Ola Adeyemi")
                    .email("ola.adeyemi@example.com")
                    .vip(true)
                    .checkedIn(false)
                    .build());

            repository.save(Attendee.builder()
                    .fullName("Sarah Thompson")
                    .email("sarah.thompson@example.com")
                    .vip(false)
                    .checkedIn(false)
                    .build());
        };
    }
}
