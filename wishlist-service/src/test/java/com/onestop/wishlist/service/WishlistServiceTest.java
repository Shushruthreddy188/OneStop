package com.onestop.wishlist.service;

import com.onestop.wishlist.client.CatalogClient;
import com.onestop.wishlist.repo.WishlistItemRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class WishlistServiceTest {
    @Test
    void addingAnExistingProductIsIdempotentAndDoesNotCallCatalog() {
        WishlistItemRepository repo = mock(WishlistItemRepository.class);
        CatalogClient catalog = mock(CatalogClient.class);
        when(repo.existsByCustomerIdAndProductId(4L, 8L)).thenReturn(true);
        when(repo.findByCustomerIdOrderByIdDesc(4L)).thenReturn(List.of());

        var result = new WishlistService(repo, catalog).add(4L, 8L);

        assertThat(result.count()).isZero();
        verifyNoInteractions(catalog);
        verify(repo, never()).save(any());
    }
}
