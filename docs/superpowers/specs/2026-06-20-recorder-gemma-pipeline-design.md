# Recorder Gemma Pipeline Refactor Design

## Goal

Reduce perceived wait time after recording while keeping the result screen's default tab on AI summary. Store the transcript as the proofread version, then generate summary and keywords asynchronously enough that the result screen can show progress instead of blocking all navigation.

## Current Problem

The recorder currently runs Gemma work in a fully blocking sequence:

1. Split audio into 30 second chunks.
2. Run STT for each chunk.
3. Run proofreading on the full transcript.
4. Run keyword extraction.
5. Run summary generation.
6. Save the note and enter the result screen.

For a recording with `N` chunks, this creates `N + 3` Gemma calls before the user can see the result. The model may be E2B, but repeated conversations and serial calls dominate the user experience.

## Target Flow

The new flow is:

1. Split audio into chunks and run STT per chunk.
2. Join chunk transcripts.
3. Run one full-transcript proofread pass.
4. Save the audio and proofread transcript immediately.
5. Navigate to the result screen with the default AI summary tab still selected.
6. Show a generating state in the AI summary tab.
7. Run one combined summary and keyword generation pass.
8. Persist summary and keywords, then refresh the result screen state.

For `N` chunks, this makes the blocking path `N + 1` calls before the result screen, and the total pipeline `N + 2` calls. This preserves transcript quality better than proofreading each chunk independently because the proofreader can see full context.

## UI Behavior

`RecordResultScreen` keeps `selectedTab = 0`, so the first visible tab remains AI summary.

The AI summary tab supports these display states:

- `Generating`: transcript is ready, summary and keywords are still being generated.
- `Success`: summary and keywords are ready.
- `Error`: summary generation failed and the existing regenerate action is available.
- `NoSpeech`: STT produced no usable transcript.

The script tab shows the proofread transcript. Users can still switch to it while summary is generating.

## Domain Changes

Add a combined analysis use case:

- `AnalyzeTranscriptWithGemmaUseCase`
- Input: proofread transcript text.
- Output: `summaryText` and `keywords`.
- Prompt requests exactly 3 summary bullets and 5 comma-separated keywords in a stable parseable format.

Keep STT and proofreading separate:

- `SttWithGemmaUseCase` continues to transcribe chunks.
- `ProofreadWithGemmaUseCase` runs once on the joined transcript.

Do not proofread each chunk. Chunk-level proofreading risks awkward boundaries between chunks.

## Persistence

The first save after recording creates the voice note with:

- audio path
- duration
- proofread transcript
- blank summary
- empty keywords
- `summaryStatus = SummaryStatus.GENERATING`

When analysis completes, update:

- summary text
- keywords
- `summaryStatus = SummaryStatus.SUCCESS`

If analysis fails, keep the transcript and set:

- blank summary
- empty keywords
- `summaryStatus = SummaryStatus.FAIL`

If STT is blank, save with:

- blank transcript
- blank summary
- empty keywords
- `summaryStatus = SummaryStatus.NONE`

## State Model

Extend `SummaryStatus` with `GENERATING` if it does not already exist.

Map result loading like this:

- blank transcript -> `NoSpeech`
- `summaryStatus == GENERATING` -> content state with summary display `Generating`
- `summaryStatus == FAIL` or blank summary -> `SummaryError`
- `summaryStatus == SUCCESS` and summary present -> `Success`

Existing title, folder move, delete, audio playback, script edit, and regenerate behavior should continue to work for all content states where applicable.

## Background Work

The first implementation can trigger summary generation from `RecordViewModel` after the initial save, because that is the smallest change from the current flow. If app process death during generation becomes important, move analysis into WorkManager in a later step.

`RecordResultViewModel` should be able to refresh the note after analysis completes. A simple first version can reload when the user lands on the screen and after regeneration. A better version can observe the voice note and related summary/keywords from Room.

## Error Handling

STT failure with no text should remain `NoSpeech`.

Proofreading failure should fall back to the raw STT transcript and still save the note.

Analysis failure should not block result screen access. The AI summary tab shows the existing error/regenerate affordance.

Prompt parsing failure should be treated as analysis failure unless a usable summary can be recovered safely.

## Testing

Add focused tests where practical:

- Parser test for combined summary and keywords output.
- State mapping test for `GENERATING`, `SUCCESS`, `FAIL`, and `NONE`.
- ViewModel-level test if existing test setup supports coroutine dispatchers and fake use cases.

Manual verification:

- Record short speech and confirm result screen opens before summary completes.
- Confirm default tab is AI summary.
- Confirm AI summary tab shows generating state, then updates to summary.
- Confirm script tab contains the proofread transcript.
- Confirm summary failure still allows regenerate.

## Out Of Scope

- Changing chunk duration from 30 seconds.
- Parallel STT processing.
- Replacing Gemma or LiteRT-LM.
- WorkManager migration.
- Large UI redesign.
