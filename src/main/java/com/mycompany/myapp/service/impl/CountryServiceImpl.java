package com.mycompany.myapp.service.impl;

import com.mycompany.myapp.domain.Country;
import com.mycompany.myapp.repository.CountryRepository;
import com.mycompany.myapp.repository.search.CountrySearchRepository;
import com.mycompany.myapp.service.CountryService;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.mycompany.myapp.domain.Country}.
 */
@Service
@Transactional
public class CountryServiceImpl implements CountryService {

    private static final Logger LOG = LoggerFactory.getLogger(CountryServiceImpl.class);

    private final CountryRepository countryRepository;

    private final CountrySearchRepository countrySearchRepository;

    public CountryServiceImpl(CountryRepository countryRepository, CountrySearchRepository countrySearchRepository) {
        this.countryRepository = countryRepository;
        this.countrySearchRepository = countrySearchRepository;
    }

    @Override
    public Country save(Country country) {
        LOG.debug("Request to save Country : {}", country);
        country = countryRepository.save(country);
        countrySearchRepository.index(country);
        return country;
    }

    @Override
    public Country update(Country country) {
        LOG.debug("Request to update Country : {}", country);
        country = countryRepository.save(country);
        countrySearchRepository.index(country);
        return country;
    }

    @Override
    public Optional<Country> partialUpdate(Country country) {
        LOG.debug("Request to partially update Country : {}", country);

        return countryRepository
            .findById(country.getId())
            .map(existingCountry -> {
                if (country.getCountryName() != null) {
                    existingCountry.setCountryName(country.getCountryName());
                }

                return existingCountry;
            })
            .map(countryRepository::save)
            .map(savedCountry -> {
                countrySearchRepository.index(savedCountry);
                return savedCountry;
            });
    }

    @Override
    @Transactional(readOnly = true)
    public List<Country> findAll() {
        LOG.debug("Request to get all Countries");
        return countryRepository.findAll();
    }

    /**
     *  Get all the countries where Location is {@code null}.
     *  @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<Country> findAllWhereLocationIsNull() {
        LOG.debug("Request to get all countries where Location is null");
        return StreamSupport.stream(countryRepository.findAll().spliterator(), false)
            .filter(country -> country.getLocation() == null)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Country> findOne(Long id) {
        LOG.debug("Request to get Country : {}", id);
        return countryRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Country : {}", id);
        countryRepository.deleteById(id);
        countrySearchRepository.deleteFromIndexById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Country> search(String query) {
        LOG.debug("Request to search Countries for query {}", query);
        try {
            return StreamSupport.stream(countrySearchRepository.search(query).spliterator(), false).toList();
        } catch (RuntimeException e) {
            throw e;
        }
    }
}
