package de.adorsys.multibanking.web;

import de.adorsys.multibanking.domain.*;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.exception.SyncInProgressException;
import de.adorsys.multibanking.pers.spi.repository.BankAccessRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.BankAccountRepositoryIf;
import de.adorsys.multibanking.pers.spi.repository.BookingRepositoryIf;
import de.adorsys.multibanking.service.BookingService;
import de.adorsys.multibanking.web.mapper.BankApiMapper;
import de.adorsys.multibanking.web.mapper.BookingMapper;
import de.adorsys.multibanking.web.model.BankApiTO;
import de.adorsys.multibanking.web.model.BookingTO;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Timed("booking")
@Tag(name = "Booking")
@AllArgsConstructor
@UserResource
@RestController
@RequestMapping(path = "api/v1/bankaccesses/{accessId}/accounts/{accountId}/bookings")
public class BookingController {

    private final BookingMapper bookingMapper;
    private final BankApiMapper bankApiMapper;
    private final BookingService bookingService;
    private final BookingRepositoryIf bookingRepository;
    private final BankAccessRepositoryIf bankAccessRepository;
    private final BankAccountRepositoryIf bankAccountRepository;
    private final Principal principal;

    @SuppressWarnings("unchecked")
    @Operation(description = "Read account bookings", security = {
        @SecurityRequirement(name = "multibanking_auth", scopes = "openid")})
    @GetMapping
    public CollectionModel<BookingTO> getBookings(@PathVariable String accessId,
                                                  @PathVariable String accountId,
                                                  @RequestParam(required = false) BankApiTO bankApi,
                                                  @RequestParam(required = false) List<String> ids,
                                                  @PageableDefault(size = 20, sort = "valutaDate", direction =
                                                Sort.Direction.DESC) Pageable pageable,
                                                  PagedResourcesAssembler assembler) {
        checkBankAccountExists(accessId, accountId);

        if (bankAccountRepository.getSyncStatus(accountId) == BankAccount.SyncStatus.SYNC) {
            throw new SyncInProgressException(accountId);
        }

        return Optional.ofNullable(ids)
            .map(strings -> {
                Iterable<BookingEntity> bookingEntities = bookingService.getBookingsById(principal.getName(), ids);
                return CollectionModel.of(bookingMapper.toBookingTOs(bookingEntities));
            })
            .orElseGet(() -> {
                Page<BookingEntity> bookingEntities = bookingService.getBookingsPageable(pageable,
                    principal.getName(), accessId, accountId, bankApiMapper.toBankApi(bankApi));
                return assembler.toModel(bookingEntities.map(bookingMapper::toBookingTO));
            });
    }

    @Operation(description = "Read account bookings search index", security = {
        @SecurityRequirement(name = "multibanking_auth", scopes = "openid")})
    @GetMapping("/index")
    public EntityModel<BookingsIndexEntity> getBookingsIndex(@PathVariable String accessId,
                                                             @PathVariable String accountId) {
        checkBankAccountExists(accessId, accountId);

        if (bankAccountRepository.getSyncStatus(accountId) == BankAccount.SyncStatus.SYNC) {
            throw new SyncInProgressException(accountId);
        }

        BookingsIndexEntity bookingsIndexEntity = bookingService.getSearchIndex(principal.getName(), accountId)
            .orElseThrow(() -> new ResourceNotFoundException(BookingsIndexEntity.class, accountId));

        return EntityModel.of(bookingsIndexEntity);
    }

    @Operation(description = "Download bookings", security = {
        @SecurityRequirement(name = "multibanking_auth", scopes = "openid")})
    @GetMapping(path = "/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public HttpEntity<String> downloadBookings(@PathVariable String accessId, @PathVariable String accountId) {
        checkBankAccountExists(accessId, accountId);

        String bookingsAsCSV = bookingService.getBookingsCsv(principal.getName(), accessId, accountId);

        return ResponseEntity.ok().body(bookingsAsCSV);
    }

    @Operation(description = "Read booking", security = {
        @SecurityRequirement(name = "multibanking_auth", scopes = "openid")})
    @GetMapping("/{bookingId}")
    public EntityModel<BookingTO> getBooking(@PathVariable String accessId, @PathVariable String accountId,
                                          @PathVariable String bookingId) {
        checkBankAccountExists(accessId, accountId);

        BookingEntity bookingEntity = bookingRepository.findByUserIdAndId(principal.getName(), bookingId)
            .orElseThrow(() -> new ResourceNotFoundException(BookingEntity.class, bookingId));

        return mapToResource(bookingEntity, accessId, accountId);
    }

    private void checkBankAccountExists(String accessId, String accountId) {
        if (!bankAccessRepository.exists(accessId)) {
            throw new ResourceNotFoundException(BankAccessEntity.class, accessId);
        }
        if (!bankAccountRepository.exists(accountId)) {
            throw new ResourceNotFoundException(BankAccountEntity.class, accountId);
        }
    }

    private EntityModel<BookingTO> mapToResource(BookingEntity entity, String accessId, String accountId) {
        return EntityModel.of(bookingMapper.toBookingTO(entity),
            linkTo(methodOn(BookingController.class).getBooking(accessId, accountId, entity.getId()))
                .withSelfRel());
    }

}
