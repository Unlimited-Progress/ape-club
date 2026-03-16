import {
  FileImageOutlined,
  MessageOutlined,
  PlusOutlined,
  ShareAltOutlined,
  SmileOutlined,
  MessageTwoTone
} from '@ant-design/icons'
import { Avatar, Button, Card, Input, Popover, Tabs, message, Upload, Image } from 'antd'
import { useEffect, useRef, useState } from 'react'
import { fetchCircleList, saveMoment, getMoments } from './service'
import CommentList from './comps/comment-list'
import './index.less'

const { Meta } = Card

const { TextArea } = Input
const Circle = () => {
  const userInfoStorage = localStorage.getItem('userInfo')
  const { tokenValue = '' } = userInfoStorage ? JSON.parse(userInfoStorage) : {}

  const [hasFocus, setHasFocus] = useState(false)
  const [comment, setComment] = useState('')
  const [circleList, setCircleList] = useState([])
  const [currentSelectCircle, setCurrentSelectCircle] = useState(null)
  const [openFlag, setOpenFlag] = useState(false)
  const [imgList, setImgList] = useState([])
  const [momentList, setMomentList] = useState([])
  const [currentReplyCommentId, setCurrentReplyCommentId] = useState(undefined)
  const finished = useRef(false)
  const [pageInfo, setPageInfo] = useState({
    pageNo: 1,
    pageSize: 10
  })
  const [previewList, setPreviewList] = useState({
    list: [],
    index: 0
  })

  const toggleFocus = (flag: boolean) => {
    setHasFocus(!flag)
  }

  const onChange = e => {
    setComment(e.target.value)
  }

  const getCircleList = async () => {
    const res = await fetchCircleList()
    if (res.success && res.data?.length > 0) {
      setCircleList(res.data)
    }
  }

  useEffect(() => {
    getCircleList()
  }, [])

  useEffect(() => {
    getMomentList()
  }, [pageInfo])

  const changeCircle = selectItem => {
    setCurrentSelectCircle(selectItem)
    setOpenFlag(false)
  }

  const renderTab = () => {
    return circleList.map(item => {
      return {
        label: item.circleName,
        key: item.id,
        children: (
          <div className='pop-content'>
            {item.children.map(child => {
              return (
                <div
                  className='pop-content-item'
                  key={child.id}
                  onClick={() => changeCircle(child)}
                >
                  <img src={child.icon} className='item-img' />
                  <span className='item-name'>{child.circleName}</span>
                </div>
              )
            })}
          </div>
        )
      }
    })
  }

  const renderPopContent = () => {
    return <Tabs tabPosition='left' size='small' items={renderTab()} />
  }

  const getMomentList = async () => {
    const res = await getMoments({
      pageInfo
    })
    const concatList = momentList.concat(res.data.result)
    setMomentList(concatList)
    if (res.data.result < 10 || concatList.length >= res.data.total) {
      finished.current = true
    }
  }

  const publishMoment = async () => {
    const params: any = {
      circleId: currentSelectCircle?.id,
      content: comment
    }
    if (imgList.length) {
      params.picUrlList = imgList.map(item => item.response.data)
    }
    const res = await saveMoment(params)
    if (res.success) {
      setComment('')
      setImgList([])
      setPageInfo({
        pageNo: 1,
        pageSize: 10
      })
      finished.current = false
      return message.success('发布成功')
    }
    return message.error('有点繁忙呢，要不再试试~~~')
  }
  const uploadButton = (
    <button style={{ border: 0, background: 'none' }} type='button'>
      <PlusOutlined />
    </button>
  )
  const handleChange = ({ fileList }) => {
    setImgList(fileList)
  }

  const showReply = (item: any) => {
    if (item.id !== currentReplyCommentId) {
      setCurrentReplyCommentId(item.id)
    } else {
      setCurrentReplyCommentId(undefined)
    }
  }

  const handlePreview = (picList, index) => {
    setPreviewList({
      list: picList,
      index
    })
  }

  const scrollHandler = e => {
    const scrollTop = e.target.scrollTop // listBox 滚动条向上卷曲出去的长度，随滚动变化
    const clientHeight = e.target.clientHeight // listBox 的视口可见高度，固定不变
    const scrollHeight = e.target.scrollHeight // listBox 的整体高度，随数据加载变化
    const saveHeight = 30 // 安全距离，距离底部XX时，触发加载
    const tempVal = scrollTop + clientHeight + saveHeight
    // 如果不加入 saveHeight 安全距离，在 scrollTop + clientHeight == scrollHeight 时，触发加载
    // 加入安全距离，相当于在 scrollTop + clientHeight >= scrollHeight - 30 时，触发加载，比前者更早触发
    if (tempVal >= scrollHeight) {
      if (!finished.current) {
        setPageInfo({
          pageNo: pageInfo.pageNo + 1,
          pageSize: 10
        })
      }

      // if (!finished && !switchFlag) {
      //   // 数据加载未结束 && 未加锁
      //   setPageIndex(pageIndex + 1)
      // }
      // setSwitchFlag(true)
    }
  }

  return (
    <div className='circle-box' onScroll={scrollHandler}>
      <Card>
        <div className={`top-input-card ${hasFocus ? 'card-focus' : ''}`}>
          <TextArea
            showCount
            maxLength={1000}
            onChange={onChange}
            placeholder='与圈友们分享你得个人经验'
            style={{
              height: 120,
              resize: 'none',
              border: 'none',
              backgroundColor: '#f2f3f5',
              boxShadow: 'none'
            }}
            className='top-text-area'
            onFocus={() => toggleFocus(false)}
            onBlur={() => toggleFocus(true)}
            value={comment}
          />
          <Upload
            name='uploadFile'
            action='/oss/upload'
            listType='picture-card'
            fileList={imgList}
            withCredentials
            headers={{
              satoken: 'jichi ' + tokenValue
            }}
            data={{
              bucket: 'user',
              objectName: 'icon'
            }}
            onChange={handleChange}
          >
            {imgList.length >= 8 || imgList.length === 0 ? null : uploadButton}
          </Upload>
          <Popover
            placement='bottomLeft'
            trigger='click'
            open={openFlag}
            onOpenChange={open => setOpenFlag(open)}
            content={renderPopContent}
          >
            <div className='choose-circle'>
              {currentSelectCircle?.circleName || '选择圈子'} {'>'}
            </div>
          </Popover>
        </div>
        <div className='publish-options'>
          <div className='left-box'>
            <div>
              <SmileOutlined />
              <span style={{ marginLeft: '8px' }}>表情</span>
            </div>
            <Upload
              name='uploadFile'
              className='avatar-uploader'
              accept='image/*'
              showUploadList={false}
              withCredentials
              action='/oss/upload'
              headers={{
                satoken: 'jichi ' + tokenValue
              }}
              data={{
                bucket: 'user',
                objectName: 'icon'
              }}
              onChange={handleChange}
            >
              <div>
                <FileImageOutlined />
                <span style={{ marginLeft: '8px' }}>图片</span>
              </div>
            </Upload>
          </div>
          <div className='right-box'>
            <Button type='primary' disabled={!comment.length} onClick={publishMoment}>
              发布
            </Button>
          </div>
        </div>
      </Card>
      {momentList.map((item: any) => {
        return (
          <Card style={{ marginTop: '10px' }} bodyStyle={{ paddingBottom: 0 }} key={item.id}>
            <Meta
              avatar={<Avatar src={item.userAvatar} />}
              title={item.userName}
              description={item.content}
            />
            {item.picUrlList?.length && (
              <Image.PreviewGroup items={item.picUrlList}>
                <div className='img-list'>
                  {item.picUrlList.map((t: string) => (
                    <Image key={t} width={110} src={t} />
                  ))}
                </div>
              </Image.PreviewGroup>
            )}
            <div className='card-footer'>
              <a
                key='share'
                className='footer-item'
                onClick={() =>
                  window.open(
                    'https://service.weibo.com/share/share.php?url=' +
                      encodeURIComponent(location.href) +
                      '&title=' +
                      item.content
                  )
                }
              >
                <ShareAltOutlined />
                <span style={{ marginLeft: 8 }}>分享</span>
              </a>
              <a key='comment' className='footer-item' onClick={() => showReply(item)}>
                {currentReplyCommentId === item.id ? <MessageTwoTone /> : <MessageOutlined />}
                <span
                  style={{
                    marginLeft: 8,
                    color: item.id === currentReplyCommentId ? '#1e80ff' : ''
                  }}
                >
                  {item.replyCount}
                </span>
              </a>
            </div>
            {currentReplyCommentId === item.id && (
              <CommentList momentId={item.id} replyCount={item.replyCount} />
            )}
          </Card>
        )
      })}
    </div>
  )
}

export default Circle
