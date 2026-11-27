package com.umc.product.storage.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.umc.product.storage.domain.FileMetadata;
import com.umc.product.storage.domain.enums.FileCategory;
import com.umc.product.storage.domain.enums.StorageProvider;
import com.umc.product.support.IntegrationTestSupport;

@DisplayName("파일 메타데이터 잠금")
class FileMetadataLockIntegrationTest extends IntegrationTestSupport {

    @Autowired
    FileMetadataRepository fileMetadataRepository;

    @Autowired
    PlatformTransactionManager transactionManager;

    @Test
    @DisplayName("같은 파일의 쓰기 잠금은 앞선 트랜잭션이 끝날 때까지 대기한다")
    void waitForPreviousFileLockTransaction() throws Exception {
        fileMetadataRepository.saveAndFlush(fileMetadata("file-id"));
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch firstLocked = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        CountDownLatch secondLocked = new CountDownLatch(1);

        try {
            Future<?> first = executor.submit(() -> transactionTemplate.executeWithoutResult(status -> {
                fileMetadataRepository.findByIdForUpdate("file-id").orElseThrow();
                firstLocked.countDown();
                await(releaseFirst);
            }));
            assertThat(firstLocked.await(5, TimeUnit.SECONDS)).isTrue();

            Future<?> second = executor.submit(() -> transactionTemplate.executeWithoutResult(status -> {
                fileMetadataRepository.findByIdForUpdate("file-id").orElseThrow();
                secondLocked.countDown();
            }));

            assertThat(secondLocked.await(500, TimeUnit.MILLISECONDS)).isFalse();
            releaseFirst.countDown();
            first.get(5, TimeUnit.SECONDS);
            second.get(5, TimeUnit.SECONDS);
            assertThat(secondLocked.getCount()).isZero();
        } finally {
            releaseFirst.countDown();
            executor.shutdownNow();
        }
    }

    private void await(CountDownLatch latch) {
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("파일 잠금 해제 대기 시간이 초과되었습니다.");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("파일 잠금 대기 중 인터럽트가 발생했습니다.", exception);
        }
    }

    private FileMetadata fileMetadata(String fileId) {
        return FileMetadata.builder()
            .fileId(fileId)
            .originalFileName("evidence.png")
            .category(FileCategory.CHALLENGER_DEACTIVATION_EVIDENCE)
            .contentType("image/png")
            .fileSize(100L)
            .storageProvider(StorageProvider.AWS_S3)
            .storageKey("private/challenger-deactivation/" + fileId + ".png")
            .uploadedMemberId(1L)
            .build();
    }
}
