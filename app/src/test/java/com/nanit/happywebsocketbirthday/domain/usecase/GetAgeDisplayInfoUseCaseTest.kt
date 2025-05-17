
import com.nanit.happywebsocketbirthday.domain.model.AgeResult
import com.nanit.happywebsocketbirthday.domain.usecase.GetAgeDisplayInfoUseCase
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Test

// Sample Clock implementation for testing
class TestClock(private val fixedInstant: Instant) : Clock {
 override fun now(): Instant = fixedInstant
}

class GetAgeDisplayInfoUseCaseTest {

 private val testTimeZone = TimeZone.UTC // Use a consistent time zone for tests

 @Test
 fun `invoke with null dateOfBirth returns Default`() {
  // Arrange
  val clock = TestClock(Instant.parse("2023-01-01T12:00:00Z")) // Fixed current time
  val useCase = GetAgeDisplayInfoUseCase(clock, testTimeZone) // Pass the mock clock and testTimeZone
  val dateOfBirth: Long? = null

  // Act
  val result = useCase.invoke(dateOfBirth)

  // Assert
  assertEquals(AgeResult.Default, result)
 }

 @Test
 fun `invoke with dateOfBirth is the current time returns Months(0)`() {
  // Arrange
  // Current time: 15 Feb 2023, 10:00:00 UTC
  val fixedCurrentInstant = Instant.parse("2023-02-15T10:00:00Z")
  val clock = TestClock(fixedCurrentInstant)
  val useCase = GetAgeDisplayInfoUseCase(clock, testTimeZone)

  // Date of birth: 15 feb 2023, 00:00:00 UTC (same date)
  val dateOfBirth = LocalDateTime(2023, Month.FEBRUARY, 15, 0, 0, 0)
   .toInstant(testTimeZone)
   .toEpochMilliseconds()

  // Act
  val result = useCase.invoke(dateOfBirth)

  // Assert
  assertEquals(AgeResult.Months(0), result)
 }

 @Test
 fun `invoke with Date of birth less than 1 month ago returns Months(0)`() {
  // Arrange
  // Current time: 15 Feb 2023, 10:00:00 UTC
  val fixedCurrentInstant = Instant.parse("2023-02-15T10:00:00Z")
  val clock = TestClock(fixedCurrentInstant)
  val useCase = GetAgeDisplayInfoUseCase(clock, testTimeZone)

  // Date of birth: 1 feb 2023, 00:00:00 UTC (less than 1 month earlier)
  val dateOfBirth = LocalDateTime(2023, Month.FEBRUARY, 1, 0, 0, 0)
   .toInstant(testTimeZone)
   .toEpochMilliseconds()

  // Act
  val result = useCase.invoke(dateOfBirth)

  // Assert
  assertEquals(AgeResult.Months(0), result)
 }

 @Test
 fun `invoke when baby is exactly 1 month old returns Months(1)`() {
  // Arrange
  // Current time: 15 Feb 2023, 10:00:00 UTC
  val fixedCurrentInstant = Instant.parse("2023-02-15T10:00:00Z")
  val clock = TestClock(fixedCurrentInstant)
  val useCase = GetAgeDisplayInfoUseCase(clock, testTimeZone)

  // Date of birth: 15 Jan 2023, 00:00:00 UTC (1 month earlier)
  val dateOfBirth = LocalDateTime(2023, Month.JANUARY, 15, 0, 0, 0)
   .toInstant(testTimeZone)
   .toEpochMilliseconds()

  // Act
  val result = useCase.invoke(dateOfBirth)

  // Assert
  assertEquals(AgeResult.Months(1), result)
 }

 @Test
 fun `invoke when baby is 1 month and 1 day old returns Months(1)`() {
  // Arrange
  // Current time: 16 MAR 2023, 10:00:00 UTC
  val fixedCurrentInstant = Instant.parse("2023-03-16T10:00:00Z")
  val clock = TestClock(fixedCurrentInstant)
  val useCase = GetAgeDisplayInfoUseCase(clock, testTimeZone)

  // Date of birth: 15 feb 2023, 00:00:00 UTC (1 month and one day earlier)
  val dateOfBirth = LocalDateTime(2023, Month.FEBRUARY, 15, 0, 0, 0)
   .toInstant(testTimeZone)
   .toEpochMilliseconds()

  // Act
  val result = useCase.invoke(dateOfBirth)

  // Assert
  assertEquals(AgeResult.Months(1), result)
 }

 @Test
 fun `invoke when baby is almost 1 month old - (its the day before the birthdate) returns Months(0)`() {
  // Arrange
  // Current time: 14 feb 2023, 10:00:00 UTC
  val fixedCurrentInstant = Instant.parse("2023-02-14T10:00:00Z")
  val clock = TestClock(fixedCurrentInstant)
  val useCase = GetAgeDisplayInfoUseCase(clock, testTimeZone)

  // Date of birth: 15 feb 2023, 00:00:00 UTC (1 month + one day earlier)
  val dateOfBirth = LocalDateTime(2023, Month.JANUARY, 15, 0, 0, 0)
   .toInstant(testTimeZone)
   .toEpochMilliseconds()

  // Act
  val result = useCase.invoke(dateOfBirth)

  // Assert
  assertEquals(AgeResult.Months(0), result)
 }

 @Test
 fun `invoke when baby is 11 months old returns Months(11)`() {
  // Arrange
  // Current time: 14 Feb 2024, 10:00:00 UTC
  val fixedCurrentInstant = Instant.parse("2024-02-14T10:00:00Z")
  val clock = TestClock(fixedCurrentInstant)
  val useCase = GetAgeDisplayInfoUseCase(clock, testTimeZone)

  // Date of birth: 15 Feb 2023, 00:00:00 UTC (12 months - 1 day earlier)
  val dateOfBirth = LocalDateTime(2023, Month.FEBRUARY, 15, 0, 0, 0)
   .toInstant(testTimeZone)
   .toEpochMilliseconds()

  // Act
  val result = useCase.invoke(dateOfBirth)

  // Assert
  assertEquals(AgeResult.Months(11), result)
 }


 @Test
 fun `invoke when baby is exactly 1 year old returns Years(1)`() {
  // Arrange
  // Current time: 15 Feb 2024, 10:00:00 UTC
  val fixedCurrentInstant = Instant.parse("2024-02-15T10:00:00Z")
  val clock = TestClock(fixedCurrentInstant)
  val useCase = GetAgeDisplayInfoUseCase(clock, testTimeZone)

  // Date of birth: 15 Feb 2023, 00:00:00 UTC (12 months earlier)
  val dateOfBirth = LocalDateTime(2023, Month.FEBRUARY, 15, 0, 0, 0)
   .toInstant(testTimeZone)
   .toEpochMilliseconds()

  // Act
  val result = useCase.invoke(dateOfBirth)

  // Assert
  assertEquals(AgeResult.Years(1), result)
 }

 @Test
 fun `invoke when baby is 12 months old + 1 day returns Years(1)`() {
  // Arrange
  // Current time: 15 Feb 2024, 10:00:00 UTC
  val fixedCurrentInstant = Instant.parse("2024-02-15T10:00:00Z")
  val clock = TestClock(fixedCurrentInstant)
  val useCase = GetAgeDisplayInfoUseCase(clock, testTimeZone)

  // Date of birth: 14 Feb 2023, 00:00:00 UTC (12 months + 1 day earlier)
  val dateOfBirth = LocalDateTime(2023, Month.FEBRUARY, 14, 0, 0, 0)
   .toInstant(testTimeZone)
   .toEpochMilliseconds()

  // Act
  val result = useCase.invoke(dateOfBirth)

  // Assert
  assertEquals(AgeResult.Years(1), result)
 }


 @Test
 fun `invoke when baby is 13 months old returns Years(1)`() {
  // Arrange
  // Current time: 15 Feb 2024, 10:00:00 UTC
  val fixedCurrentInstant = Instant.parse("2024-02-15T10:00:00Z")
  val clock = TestClock(fixedCurrentInstant)
  val useCase = GetAgeDisplayInfoUseCase(clock, testTimeZone)

  // Date of birth: 15 Jan 2023, 00:00:00 UTC (13 months earlier)
  val dateOfBirth = LocalDateTime(2023, Month.JANUARY, 15, 0, 0, 0)
   .toInstant(testTimeZone)
   .toEpochMilliseconds()

  // Act
  val result = useCase.invoke(dateOfBirth)

  // Assert
  assertEquals(AgeResult.Years(1), result)
 }

 @Test
 fun `invoke when baby is just under 24 months old returns Years(1)`() {
  // Arrange
  // Current time: 16 March 2025, 10:00:00 UTC (example current date)
  val fixedCurrentInstant = Instant.parse("2025-03-16T10:00:00Z")
  val clock = TestClock(fixedCurrentInstant)
  val useCase = GetAgeDisplayInfoUseCase(clock, testTimeZone)

  // Date of birth: 17 March 2023.
  // Current date March 16 is before March 17, so 23 full months have passed.
  val dateOfBirth = LocalDateTime(2023, Month.MARCH, 17, 0, 0, 0)
   .toInstant(testTimeZone)
   .toEpochMilliseconds()

  // Act
  val result = useCase.invoke(dateOfBirth)

  // Assert
  assertEquals(AgeResult.Years(1), result)
 }

 @Test
 fun `invoke when baby is exactly 24 months old returns Years(2)`() {
  // Arrange
  // Current time: 15 March 2025, 10:00:00 UTC
  val fixedCurrentInstant = Instant.parse("2025-03-15T10:00:00Z")
  val clock = TestClock(fixedCurrentInstant)
  val useCase = GetAgeDisplayInfoUseCase(clock, testTimeZone)

  // Date of birth: 15 March 2023, 00:00:00 UTC (exactly 24 months earlier)
  val dateOfBirth = LocalDateTime(2023, Month.MARCH, 15, 0, 0, 0)
   .toInstant(testTimeZone)
   .toEpochMilliseconds()

  // Act
  val result = useCase.invoke(dateOfBirth)

  // Assert
  assertEquals(AgeResult.Years(2), result)
 }

 @Test
 fun `invoke with DOB Feb 29 (leap) and current Mar 1 (next non-leap year) returns Years(1)`() {
  // Scenario: DOB Feb 29, 2020 (Leap Year)
  // Current: Mar 1, 2021 (Non-Leap Year)
  // Expect: AgeResult.Years(1)

  // Arrange
  val fixedCurrentInstant = LocalDateTime(2021, Month.MARCH, 1, 10, 0, 0) // Non-leap year
   .toInstant(testTimeZone)
  val clock = TestClock(fixedCurrentInstant)
  val useCase = GetAgeDisplayInfoUseCase(clock, testTimeZone)

  val dateOfBirth = LocalDateTime(2020, Month.FEBRUARY, 29, 0, 0, 0) // Leap year
   .toInstant(testTimeZone)
   .toEpochMilliseconds()

  // Act
  val result = useCase.invoke(dateOfBirth)

  // Assert
  assertEquals(AgeResult.Years(1), result)
 }


 @Test
 fun `invoke with DOB Mar 1 and current Mar 1 (10 years later) returns Years(10)`() {

  // Arrange
  val fixedCurrentInstant = LocalDateTime(2025, Month.MARCH, 1, 10, 0, 0)
   .toInstant(testTimeZone)
  val clock = TestClock(fixedCurrentInstant)
  val useCase = GetAgeDisplayInfoUseCase(clock, testTimeZone)

  val dateOfBirth = LocalDateTime(2015, Month.MARCH, 1, 0, 0, 0)
   .toInstant(testTimeZone)
   .toEpochMilliseconds()

  // Act
  val result = useCase.invoke(dateOfBirth)

  // Assert
  assertEquals(AgeResult.Years(10), result)
 }
}