import { beforeEach, describe, expect, it } from 'vitest'
import { readSeen, saveSeen, stepStory, type Story } from '@/features/home/stories'

const story = (id: string, slides: number) =>
  ({ id, slides: Array.from({ length: slides }, () => ({})) }) as Story
const list = [story('a', 2), story('b', 1), story('c', 3)]

describe('stepStory', () => {
  it('листает слайды внутри сторис', () => {
    expect(stepStory(list, { story: 0, slide: 0 }, 1)).toEqual({ story: 0, slide: 1 })
    expect(stepStory(list, { story: 2, slide: 2 }, -1)).toEqual({ story: 2, slide: 1 })
  })

  it('с последнего слайда — в начало следующей, с первого — в конец предыдущей', () => {
    expect(stepStory(list, { story: 0, slide: 1 }, 1)).toEqual({ story: 1, slide: 0 })
    expect(stepStory(list, { story: 2, slide: 0 }, -1)).toEqual({ story: 1, slide: 0 })
    expect(stepStory(list, { story: 1, slide: 0 }, -1)).toEqual({ story: 0, slide: 1 })
  })

  it('за краями — null', () => {
    expect(stepStory(list, { story: 2, slide: 2 }, 1)).toBeNull()
    expect(stepStory(list, { story: 0, slide: 0 }, -1)).toBeNull()
  })
})

describe('просмотренные', () => {
  beforeEach(() => localStorage.clear())

  it('сохраняются и читаются', () => {
    saveSeen(new Set(['a', 'c']))
    expect([...readSeen()]).toEqual(['a', 'c'])
  })

  it('мусор в хранилище — пустой набор', () => {
    localStorage.setItem('stories-seen', '{oops')
    expect(readSeen().size).toBe(0)
    localStorage.setItem('stories-seen', '[1, "b"]')
    expect([...readSeen()]).toEqual(['b'])
  })
})
