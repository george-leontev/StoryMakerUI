# LivePlot Concept Specification

## Executive Summary
LivePlot is a social platform for interactive storytelling where the plot is shaped by the community in real-time. Authors publish chapters and create timed binary choices; readers vote on plot directions, and the winning choice dictates the content of the next chapter, which the author writes from scratch.

## Core Mechanics
- **Binary Choice:** At the end of key chapters, authors present exactly two options (e.g., "Attack" vs "Retreat").
- **Silent Interaction:** Readers select their path by clicking one of the two buttons. Both buttons immediately become inactive (grayed out) to confirm the selection. **No vote counts or results are shown to the reader during this phase.**
- **Live Continuation:** Once the timer expires, the final tally is revealed, showing how many people chose each path, and the author writes the next chapter based on the majority choice.

## User Personas
1. **The Live Author:** A creative individual who enjoys immediate feedback and the challenge of writing based on audience prompts. Uses their phone as their primary writing tool.
2. **The Active Reader:** A young person who wants to feel like their opinion matters in the development of a story.

## User Journey
### Reader Journey
1. Browse and read available chapters.
2. At the end of a chapter, see two choice buttons.
3. Vote (buttons gray out, results hidden).
4. Receive a notification when the voting ends.
5. Re-visit to see final results and read the continuation.
6. Leave comments and rate the story.

### Author Journey
1. Write and publish chapters from a mobile device.
2. Add a binary choice with a set duration at the end of a chapter.
3. Monitor total vote counts (visible only to the author).
4. Get notified of the winner when the timer ends.
5. Write the next chapter based on the winning choice.

## Out of Scope
- Pre-written branching paths (all continuations are written live).
- Multiple-choice polls (strictly 2 options).
- Offline reading.
